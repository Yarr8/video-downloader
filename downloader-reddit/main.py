from flask import Flask, request, send_file
from dotenv import load_dotenv
from waitress import serve
import os
import threading

import downloader
import urlCleaner


app = Flask(__name__)


# url like https://www.reddit.com/r/aww/comments/172bc65/kitty_learning_things_from_mom
@app.route('/download', methods=['POST'])
def download_video():
    data = request.get_json()
    if not data or 'url' not in data:
        return {'error': 'Missing url'}, 400

    url = data['url']
    url = urlCleaner.clean_reddit_url(url)
    print('Requested url: ' + url)

    filename = url.split('/')[-2]
    download_name = filename + '.mp4'
    full_name = 'tmp/' + filename + '.mp4'

    try:
        downloader.download(url, filename, True)
        print('Sending file: ' + full_name)

        response = send_file(
            full_name,
            mimetype='video/mp4',
            as_attachment=True,
            download_name=download_name,
            conditional=False
        )

        delete_file_later(full_name)

        return response
    except Exception as e:
        return {'error': f'Failed to download video: {e}'}, 500


def delete_file_later(path, delay=10):
    def delete():
        try:
            os.remove(path)
            print(f"Deleted {path}")
        except Exception as e:
            print(f"Failed to delete {path}: {e}")
    threading.Timer(delay, delete).start()


# downloader.download('https://www.reddit.com/r/Unexpected/comments/1lk28mq/who_are_we_hiding_from/', 'who_are_we_hiding_from', True)

load_dotenv('./../.env')

port = int(os.getenv("DOWNLOADER_REDDIT_PORT", 8081))

if __name__ == "__main__":
    print('Starting on port: ' + str(port))
    serve(app, host="0.0.0.0", port=port)
    # app.run(host="0.0.0.0", port=port)
