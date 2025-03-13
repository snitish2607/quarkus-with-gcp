from fastapi import FastAPI, File, UploadFile, Form
from google.cloud import storage
from PIL import Image
import io
import logging

app = FastAPI()

logging.basicConfig(level=logging.INFO)

BUCKET_NAME = "quarkus-with-gcp-bucket"

storage_client = storage.Client()
bucket = storage_client.bucket(BUCKET_NAME)

@app.post("/upload")
async def upload_file(file: UploadFile = File(...), filename: str = Form(...)):
    try:
        image = Image.open(io.BytesIO(await file.read()))
        image_byte_arr=io.BytesIO()
        image.save(image_byte_arr, format='WEBP', quality=85)
        image_byte_arr.seek(0)

        logging.info("Converted to WebP format")

        blob = bucket.blob(f"uploads/{filename}")
        blob.upload_from_file(image_byte_arr, content_type="image/webp")

        logging.info(f"Uploaded to GCP bucket: {filename}")

        return {"message": "File uploaded successfully", "url": f"uploads/{filename}"}
    except Exception as e:

        logging.info(f"Error while uploading file: {e}")
        return {"error" : str(e)}
    
@app.get("/test")
def check():
    return "Hello, from the Python Service"

@app.get("/")
def checktwo():
    return "Service is healthy!"
