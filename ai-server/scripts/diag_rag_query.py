"""Diagnostic: run a query against the org_1 chroma collection and print raw distances + computed scores."""

import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT))

from config.settings import get_settings
from infrastructure.chroma_client import ChromaClientWrapper
from infrastructure.embedding_client import EmbeddingClient
from infrastructure.retriever.chroma_retriever import ChromaRetriever


def distance_to_score(distance: float) -> float:
    return round(max(0.0, 1.0 - (float(distance) / 2.0)), 6)


def main() -> None:
    settings = get_settings()
    embedder = EmbeddingClient(settings)
    chroma = ChromaClientWrapper(settings)

    queries = [
        "MVTEC에 대해 알려줘",
        "데이터셋 카테고리에 대해 알려줘",
        "MVTec AD object texture",
        "검사데이터셋 카테고리 개요",
    ]

    collection = chroma.client_instance.get_collection(
        name=chroma.collection_name_for_organization(1)
    )

    print(f"collection: {collection.name} (count={collection.count()})")
    print(f"rag_min_score: {settings.rag_min_score}")
    print()

    for q in queries:
        qvec = embedder.embed_query(q)
        result = collection.query(
            query_embeddings=[qvec],
            n_results=5,
            where={"$and": [{"organizationId": 1}, {"document_status": "PUBLISHED"}]},
            include=["documents", "metadatas", "distances"],
        )
        distances = (result.get("distances") or [[]])[0]
        ids = (result.get("ids") or [[]])[0]

        print(f"--- query: {q!r}")
        if not ids:
            print("  no rows returned")
            continue
        for cid, dist in zip(ids, distances):
            score = distance_to_score(dist)
            passes = "PASS" if score >= settings.rag_min_score else "DROP"
            print(f"  {cid:<32} distance={dist:.4f}  score={score:.4f}  [{passes}]")
        print()


if __name__ == "__main__":
    main()
