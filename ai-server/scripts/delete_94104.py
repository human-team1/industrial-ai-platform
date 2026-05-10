import sys
from pathlib import Path
sys.path.insert(0, str(Path("ai-server").resolve()))
from config.settings import get_settings
from infrastructure.chroma_client import ChromaClientWrapper

settings = get_settings()
chroma = ChromaClientWrapper(settings)
collection = chroma.client_instance.get_collection(name=chroma.collection_name_for_organization(1))

print(f"before count: {collection.count()}")
sample = collection.peek(limit=1)
print(f"metadata keys: {list((sample.get('metadatas') or [{}])[0].keys()) if sample.get('metadatas') else 'none'}")

# 시도 1: snake_case
for key in ["document_version_id", "documentVersionId", "doc_version_id"]:
    res = collection.get(where={key: 94104}, limit=100)
    if res.get("ids"):
        print(f"matched with key={key}, ids={len(res['ids'])}")
        collection.delete(where={key: 94104})
        break
else:
    # ID prefix로 fallback
    all_ids = collection.get(limit=200).get("ids", [])
    prefix_ids = [i for i in all_ids if i.startswith("docver-94104-")]
    print(f"prefix match ids: {len(prefix_ids)}")
    if prefix_ids:
        collection.delete(ids=prefix_ids)

print(f"after count: {collection.count()}")
