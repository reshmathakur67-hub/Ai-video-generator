from fastapi import FastAPI, BackgroundTasks, HTTPException
from fastapi.staticfiles import StaticFiles
from pydantic import BaseModel
from pathlib import Path
from typing import Optional
import uuid

app = FastAPI(
    title="AI Video Studio Backend",
    version="1.0.0"
)

# Generated videos will be stored here later.
OUTPUT_DIR = Path("outputs")
OUTPUT_DIR.mkdir(exist_ok=True)

app.mount("/videos", StaticFiles(directory=str(OUTPUT_DIR)), name="videos")

# Temporary in-memory job storage.
# Later we can replace this with Redis / database.
jobs = {}


class VideoRequest(BaseModel):
    prompt: str
    enhancedPrompt: Optional[str] = ""
    mode: Optional[str] = "TEXT_TO_VIDEO"
    durationSeconds: Optional[int] = 8
    aspectRatio: Optional[str] = "9:16"
    style: Optional[str] = "Cinematic"
    quality: Optional[str] = "720p"
    cameraMotion: Optional[str] = "Auto"
    motionStrength: Optional[str] = "Medium"
    negativePrompt: Optional[str] = ""
    keepCharacterConsistent: Optional[bool] = False
    voice: Optional[dict] = None


def generate_video_job(job_id: str, request: VideoRequest):
    """
    The self-hosted AI video model will be connected here next.
    For now this verifies that the Android app and backend job
    system can communicate correctly.
    """

    jobs[job_id]["status"] = "PROCESSING"

    try:
        # HunyuanVideo / another self-hosted model
        # will generate the actual MP4 here in the next step.

        jobs[job_id]["status"] = "MODEL_NOT_CONNECTED"

    except Exception as exc:
        jobs[job_id]["status"] = "FAILED"
        jobs[job_id]["error"] = str(exc)


@app.get("/")
def home():
    return {
        "status": "online",
        "service": "AI Video Studio Backend"
    }


@app.get("/health")
def health():
    return {"status": "healthy"}


@app.post("/api/video/generate")
def generate_video(
    request: VideoRequest,
    background_tasks: BackgroundTasks
):
    if not request.prompt.strip():
        raise HTTPException(
            status_code=400,
            detail="Prompt cannot be empty"
        )

    job_id = str(uuid.uuid4())

    jobs[job_id] = {
        "jobId": job_id,
        "status": "QUEUED",
        "videoUrl": None,
        "error": None
    }

    background_tasks.add_task(
        generate_video_job,
        job_id,
        request
    )

    return {
        "jobId": job_id,
        "status": "QUEUED"
    }


@app.get("/api/video/status/{job_id}")
def video_status(job_id: str):
    job = jobs.get(job_id)

    if job is None:
        raise HTTPException(
            status_code=404,
            detail="Video job not found"
        )

    return job
