import re


def clean_reddit_url(url):
    """Strips tracking parameters and unnecessary parts from a Reddit URL."""
    # Remove URL parameters (everything after ?)
    base_url = url.split('?')[0]

    # Remove 'https://', 'http://', and 'www.' prefixes if present
    cleaned_url = re.sub(r'^https?://(www\.)?', '', base_url)

    # Ensure the URL starts with 'reddit.com'
    if not cleaned_url.startswith('reddit.com'):
        # Handle cases where the domain might be different (e.g., old.reddit.com)
        parts = cleaned_url.split('/')
        if len(parts) > 2:
            cleaned_url = 'reddit.com/' + '/'.join(parts[3:])
        else:
            cleaned_url = 'reddit.com'

    return 'https://www.' + cleaned_url
