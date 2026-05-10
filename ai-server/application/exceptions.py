class AppException(Exception):
    def __init__(self, status_code: int, title: str, detail: str, code: str) -> None:
        self.status_code = status_code
        self.title = title
        self.detail = detail
        self.code = code
        super().__init__(detail)
