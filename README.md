API Usage
The application exposes a RESTful API to interact with the PDF segmentation service. Below are detailed instructions for each endpoint, including examples.

1. Upload PDF (POST /api/pdfs)
Endpoint: /api/pdfs

Uploads a PDF file and specifies the number of segments to create.

Method: POST
URL: http://localhost:8080/api/pdfs
Request Body (form-data):
file: The PDF file to upload.
cuts: Number of segments to create.

2. Retrieve Segmented PDFs (GET /api/pdfs/{id}/segments)
Endpoint: /api/pdfs/{id}/segments

Retrieves the segmented PDF files.

Method: GET
URL: http://localhost:8080/api/pdfs/{id}/segments
Replace {id} with the ID of the uploaded PDF.
Example Using Postman:

Send a GET request to http://localhost:8080/api/pdfs/1/segments.
The response will be a ZIP file containing the segmented PDF files.

3. Get Metadata (GET /api/pdfs/{id}/metadata)
Endpoint: /api/pdfs/{id}/metadata

Retrieves metadata about the processed PDF.

Method: GET
URL: http://localhost:8080/api/pdfs/{id}/metadata
Replace {id} with the ID of the uploaded PDF.

4. Update Segmentation (PUT /api/pdfs/{id})
Endpoint: /api/pdfs/{id}

Updates the segmentation parameters of the PDF.

Method: PUT
URL: http://localhost:8080/api/pdfs/{id}
Replace {id} with the ID of the uploaded PDF.
Request Body (form-data):
cuts: New number of segments.


5. Delete Processed PDF (DELETE /api/pdfs/{id})
Endpoint: /api/pdfs/{id}

Deletes the processed PDF and its segments.

Method: DELETE
URL: http://localhost:8080/api/pdfs/{id}
Replace {id} with the ID of the uploaded PDF.
