from fastapi import FastAPI, File, UploadFile, HTTPException
from fastapi.responses import JSONResponse
import os
from concurrent.futures import ProcessPoolExecutor
import shutil
import mediapipe as mp
import cv2
from mediapipe.python import solutions
from ultralytics import YOLO
from mediapipe.framework.formats import landmark_pb2
import numpy as np
import moviepy.editor as mpv
import speech_recognition as sr
from multiprocessing import Process, Manager
import threading

app = FastAPI()
UPLOAD_DIR = "uploaded_videos"

# Создание директории для загрузки видео, если она не существует
if not os.path.exists(UPLOAD_DIR):
    os.makedirs(UPLOAD_DIR)


def draw_landmarks_on_image(rgb_image, detection_result):
    MARGIN = 10  # pixels
    FONT_SIZE = 1
    FONT_THICKNESS = 1
    HANDEDNESS_TEXT_COLOR = (88, 205, 54)  # vibrant green
    hand_landmarks_list = detection_result.hand_landmarks
    handedness_list = detection_result.handedness
    annotated_image = np.copy(rgb_image)

    # Loop through the detected hands to visualize.
    for idx in range(len(hand_landmarks_list)):
        hand_landmarks = hand_landmarks_list[idx]
        handedness = handedness_list[idx]

        # Draw the hand landmarks.
        hand_landmarks_proto = landmark_pb2.NormalizedLandmarkList()
        hand_landmarks_proto.landmark.extend([
            landmark_pb2.NormalizedLandmark(x=landmark.x, y=landmark.y, z=landmark.z) for landmark in hand_landmarks
        ])
        solutions.drawing_utils.draw_landmarks(
            annotated_image,
            hand_landmarks_proto,
            solutions.hands.HAND_CONNECTIONS,
            solutions.drawing_styles.get_default_hand_landmarks_style(),
            solutions.drawing_styles.get_default_hand_connections_style())

        # Get the top left corner of the detected hand's bounding box.
        height, width, _ = annotated_image.shape
        x_coordinates = [landmark.x for landmark in hand_landmarks]
        y_coordinates = [landmark.y for landmark in hand_landmarks]
        text_x = int(min(x_coordinates) * width)
        text_y = int(min(y_coordinates) * height) - MARGIN

        # Draw handedness (left or right hand) on the image.
        cv2.putText(annotated_image, f"{handedness[0].category_name}",
                    (text_x, text_y), cv2.FONT_HERSHEY_DUPLEX,
                    FONT_SIZE, HANDEDNESS_TEXT_COLOR, FONT_THICKNESS, cv2.LINE_AA)

    return annotated_image


class Analyzer:
    def __init__(self):
        self.faceCascade = cv2.CascadeClassifier(cv2.data.haarcascades + 'haarcascade_frontalface_default.xml')

        self.mpHands = mp.solutions.hands
        self.hands = self.mpHands.Hands(max_num_hands=2, min_detection_confidence=0.7)

        self.sleeveless_detector = YOLO("C:/Users/user/Desktop/AI Awards/tshirt_detector.pt")
        self.smile_classifier = YOLO("C:/Users/user/Desktop/AI Awards/emo_detector.pt")

    def face_exist(self, image) -> bool:
        gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

        faces = self.faceCascade.detectMultiScale(gray, 1.3, 5)

        return len(faces) > 0

    def hands_exist(self, image) -> bool:
        # Здесь выполняется первая обработка видео

        result = self.hands.process(image)

        return not result.multi_hand_landmarks is None
    def sleeveless_exist(self, image) -> bool:
        result = self.sleeveless_detector(image, conf=0.9)[0].boxes.xyxy

        return len(result) > 0

    def smile_exist(self, image) -> bool:
        # преобразуем кадр в оттенки серого
        gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

        faces = self.faceCascade.detectMultiScale(gray,
                                             scaleFactor=1.1,
                                             minNeighbors=5,
                                             minSize=(60, 60),
                                             flags=cv2.CASCADE_SCALE_IMAGE)

        if len(faces) != 0:
            (x, y, w, h) = faces[0]

            face = gray[y:y + h, x:x + w]

            results = self.smile_classifier(face)
            names_dict = results[0].names
            probs = results[0].probs.data.tolist()
            smile = names_dict[np.argmax(probs)] == "happy"

            return smile
        else:
            return False








def analize_video(video_path: str, return_dict):
    # Для детектирования лиц используем каскады Хаара

    analizer = Analyzer()

    cap = cv2.VideoCapture(video_path)

    frame_count = 0
    mistakes_count = {"sleeveless": 0, "hands": 0, "face": 0, "smile" : 0}

    while True:

        ret, frame = cap.read()
        if ret:
            frame_count += 1

            mistakes_count["sleeveless"] += int(analizer.sleeveless_exist(frame))
            mistakes_count["hands"] += int(analizer.hands_exist(frame))
            mistakes_count["face"] += int(analizer.face_exist(frame))
            mistakes_count["smile"] += int(analizer.smile_exist(frame))

        else:
            break

    sleeveless_percent = (mistakes_count["sleeveless"] / frame_count) * 100
    hands_percent = (mistakes_count["hands"] / frame_count) * 100
    face_percent = (mistakes_count["face"] / frame_count) * 100
    smile_percent = (mistakes_count["smile"] / frame_count) * 100

    recommendation = ""

    print("sleeveless_percent", sleeveless_percent)
    print("hands_percent", hands_percent)
    print("face_percent", face_percent)
    print("smile_percent", smile_percent)
    if sleeveless_percent > 80:
        recommendation += "Если Вы сидели в майке без рукавов, то для более официально делового стиля надевать рубашку или поло.\n"
    if hands_percent < 15:
        recommendation += "Во время презентации стоит использовать жестикуляцию руками. Жесты помогают спикеру сделать речь выразительной и убедительной\n"
    if face_percent < 50:
        recommendation += "Во время презентации рекомендуется держать голову ровно и смотреть всегда в камеру\n"
    if smile_percent < 60:
        recommendation += "Больше улыбайтесь!\n"

    return_dict['video_rec'] = recommendation


def check_parasite_words(text):
    # Список слов-паразитов
    parasite_words = ["ну", "как бы", "значит", "типа", "короче", "в общем", "вот", "это самое", "в принципе",
                      "короче говоря", "так сказать", "так вот", "да", "нет", "кстати"]

    # Разделим текст на слова
    words = text.lower().split()

    # Подсчитаем количество слов-паразитов
    parasite_count = sum(1 for word in words if word in parasite_words)

    # Общие количество слов
    total_words = len(words)

    # Процент слов-паразитов
    parasite_percentage = (parasite_count / total_words) * 100

    return True if parasite_percentage > 30 else False


def analize_text(video_path: str, return_dict):
    # Здесь выполняется вторая обработка видео
    # Load the video
    video = mpv.VideoFileClip(video_path)

    # Extract the audio from the video
    audio_file = video.audio
    audio_file.write_audiofile("123.wav")

    # Initialize recognizer
    r = sr.Recognizer()

    sr.LANGUAGE = 'ru-RU'
    # Load the audio file
    with sr.AudioFile("123.wav") as source:
        data = r.record(source)

    # Convert speech to text
    text = r.recognize_google(data, language='ru-RU')

    # Print the text
    print("\nThe resultant text from video is: \n")
    print(text)

    if check_parasite_words(text):
        return_dict["text_rec"] = "В вашей речи слишком много слов-паразитов.\n"
    else:
        return_dict["text_rec"] = ""


def start_analize(file_location) -> str:

    return_dict = {}

    #p1 = Process(target=analize_video, args=(file_location, return_dict))
    #p2 = Process(target=analize_text, args=(file_location, return_dict))

    analize_video(file_location, return_dict)
    analize_text(file_location, return_dict)
    print(return_dict)
    # p1.start()
    # p2.start()
    #
    # p1.join()
    # p2.join()

    recommendation = ""
    for k, v in return_dict.items():
        if len(return_dict[k]) > 0:
            recommendation += return_dict[k]

    return recommendation


@app.post("/upload/")
async def upload_video(file: UploadFile = File(...)):
    try:
        # Сохранение загруженного файла
        file_location = os.path.join(UPLOAD_DIR, f"{file.filename}")
        with open(file_location, "wb") as buffer:
            shutil.copyfileobj(file.file, buffer)

        recommendation = start_analize(file_location)

        return JSONResponse(status_code=200, content={"recommendation": recommendation})

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"An error occurred: {e}")


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="0.0.0.0", port=80)
