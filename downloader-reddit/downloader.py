import requests
import json
import os
from xml.dom import minidom
import moviepy as mpe
from pathlib import Path

temp_json_file_path = 'tmp/temp_json.json'
temp_mpd_file_path = 'tmp/temp_dash_mpd.mpd'


def load_json(reddit_thread_url):
    url = reddit_thread_url + ".json"
    with open(temp_json_file_path, 'w') as f:
        f.write(requests.get(url, headers={'User-agent': 'Yarr video downloader 0.2'}).content.decode())


def load_dash_mdp(dash_url):
    with open(temp_mpd_file_path, 'w') as f:
        f.write(requests.get(dash_url, headers={'User-agent': 'Yarr video downloader 0.2'}).content.decode())


def get_video_url(json_data):
    return json_data[0]['data']['children'][0]['data']['secure_media']['reddit_video']['fallback_url']


def get_dash_url(json_data):
    return json_data[0]['data']['children'][0]['data']['secure_media']['reddit_video']['dash_url']


def download(reddit_thread_url, output_name, with_sound):
    load_json(reddit_thread_url)
    json_data = json.loads(open(temp_json_file_path, 'r').read())

    if 'error' in json_data:
        # ah shit, here we go again
        print('Something went wrong, error: ' + str(json_data['error']))
        return

    # Path(output_folder).mkdir(parents=True, exist_ok=True)

    # get title and description
    title = json_data[0]['data']['children'][0]['data']['title']
    self_text = json_data[0]['data']['children'][0]['data']['selftext']
    # and write it in separate file
    # with open(f"{output_folder}/{output_name}.txt", 'w') as f:
    #     f.write(title + '\n' + self_text)
    # get video url
    video_url = get_video_url(json_data)

    # getting audio url:
    # dash_url leads to -> dash.mpd which contains -> audio url postfix which can be used ->
    # replace video part from video_url with audio_postfix
    dash_url = get_dash_url(json_data)
    load_dash_mdp(dash_url)
    dash_data = minidom.parse(temp_mpd_file_path)
    # hard coded long ass XML path to audio postfix
    # need to find AdaptationSet responsible for audio
    have_audio = False
    if with_sound:
        for set in dash_data.getElementsByTagName('Period')[0].getElementsByTagName('AdaptationSet'):
            if set.getAttribute('contentType') == 'audio':
                audio_postfix = set.getElementsByTagName('Representation')[0].getElementsByTagName('BaseURL')[0].firstChild.nodeValue
                have_audio = True
    # generate nice file name from the last url part
    last_index = video_url.rfind('/')
    if have_audio:
        audio_url = video_url[0:last_index + 1] + audio_postfix

    # load video
    temp_video_file_path = "tmp/temp_video.mp4"
    with open(temp_video_file_path, 'wb') as video_file:
        video_file.write(requests.get(video_url, headers={'User-agent': 'Yarr video downloader 0.2'}).content)
    video_clip = mpe.VideoFileClip(temp_video_file_path)

    # load audio
    if have_audio:
        temp_audio_file_path = "tmp/temp_audio.mp4"
        with open(temp_audio_file_path, 'wb') as audio_file:
            audio_file.write(requests.get(audio_url, headers={'User-agent': 'Yarr video downloader 0.2'}).content)
        audio_clip = mpe.AudioFileClip(temp_audio_file_path)
    # merge into one .mp4
    output_file_name = f"tmp/{output_name}.mp4"
    if have_audio:
        video_clip.audio = audio_clip
        final_clip = video_clip
    else:
        final_clip = video_clip

    print(f"Saving: {output_file_name}")
    final_clip.write_videofile(output_file_name, logger=None)

    # remove temp files
    # os.remove(temp_video_file_path)
    # if have_audio:
    #     os.remove(temp_audio_file_path)
    # os.remove(temp_json_file_path)
    # os.remove(temp_mpd_file_path)
