# Feature: front-pages
Branch: feature/front-pages
Plain HTML, CSS and JavaScript.

## How to run the pages
Make sure you have Python installed on your machine as this relies on Python's inbuilt in web-server to show how the web pages look as no back-end server is hooked up.
To run the code:
```
cd frontend
python3 -m http.server 8000
```
Then open http://localhost:8000.
The pages currently talk to a fake backend kept in the browser (USE_MOCK is true in js/api.js).
When the Java API is running, set USE_MOCK to false and check API_BASE.
