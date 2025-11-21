from pydantic import BaseModel
from typing import List

class UploadFaceRequest(BaseModel):
    url_list: List[str]