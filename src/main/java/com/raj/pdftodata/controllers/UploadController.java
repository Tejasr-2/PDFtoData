package com.raj.pdftodata.controllers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.google.cloud.documentai.v1beta3.Document;
import com.google.cloud.documentai.v1beta3.DocumentProcessorServiceClient;
import com.google.cloud.documentai.v1beta3.ProcessRequest;
import com.google.cloud.documentai.v1beta3.ProcessResponse;
import com.google.protobuf.ByteString;

@Controller
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 10, // 10 MB
		maxFileSize = 1024 * 1024 * 50, // 50 MB
		maxRequestSize = 1024 * 1024 * 100) // 100 MB
public class UploadController {

	static Map<String, String> outputMap = new HashMap<>();

	String fileName = null;
//	@Autowired
//	private Storage storage;

	@GetMapping({ "/", "/upload" })
	public String loadForm(Model model) {

		return "uploadFiles";
	}

	@PostMapping("/upload")
	public String handleUploadButton(Model model, HttpServletRequest req) {

		outputMap.clear();
		
		String fileName = null;
		try {

			for (Part part : req.getParts()) {
				fileName = getFileName(part);
				part.write("D:\\UploadedFiles" + File.separator + fileName);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		String filePath = "D:\\UploadedFiles\\" + fileName;

		try {
			processDocument(filePath);
		} catch (IOException e) {

			e.printStackTrace();
		} catch (InterruptedException e) {

			e.printStackTrace();
		} catch (ExecutionException e) {

			e.printStackTrace();
		} catch (TimeoutException e) {

			e.printStackTrace();
		}

		model.addAttribute("fechedData", outputMap);
		model.addAttribute("status", "File uploaded to : D:\\UploadedFiles\\" + fileName);

		return "uploadFiles";
	}

	private String getFileName(Part part) {
		String contentDisp = part.getHeader("content-disposition");
		// System.out.println("content-disposition header= "+contentDisp);
		String[] tokens = contentDisp.split(";");
		for (String token : tokens) {
			if (token.trim().startsWith("filename")) {
				return token.substring(token.indexOf("=") + 2, token.length() - 1);
			}
		}
		return "";
	}

	public void processDocument(String filePath)
			throws IOException, InterruptedException, ExecutionException, TimeoutException {

		String projectId = "440864595877";
		String location = "us"; // Format is "us" or "eu".
		String processerId = "a1fdcc082c481fc3";
		//filePath = "C:\\Users\\Tejas\\Downloads\\invoice.pdf";
		processDocument(projectId, location, processerId, filePath);
	}

	public static void processDocument(String projectId, String location, String processorId, String filePath)
			throws IOException, InterruptedException, ExecutionException, TimeoutException {
		// Initialize client that will be used to send requests. This client only needs
		// to be created
		// once, and can be reused for multiple requests. After completing all of your
		// requests, call
		// the "close" method on the client to safely clean up any remaining background
		// resources.
		try (DocumentProcessorServiceClient client = DocumentProcessorServiceClient.create()) {
			// The full resource name of the processor, e.g.:
			// projects/project-id/locations/location/processor/processor-id
			// You must create new processors in the Cloud Console first
			String name = String.format("projects/%s/locations/%s/processors/%s", projectId, location, processorId);

			// Read the file.
			byte[] imageFileData = Files.readAllBytes(Paths.get(filePath));

			// Convert the image data to a Buffer and base64 encode it.
			ByteString content = ByteString.copyFrom(imageFileData);

			Document document = Document.newBuilder().setContent(content).setMimeType("application/pdf").build();

			// Configure the process request.
			ProcessRequest request = ProcessRequest.newBuilder().setName(name).setDocument(document).build();

			// Recognizes text entities in the PDF document
			ProcessResponse result = client.processDocument(request);
			Document documentResponse = result.getDocument();

			// Get all of the document text as one big string
			String text = documentResponse.getText();

			// Read the text recognition output from the processor
//		      System.out.println("The document contains the following paragraphs:");
			Document.Page firstPage = documentResponse.getPages(0);
//		      List<Document.Page.Paragraph> paragraphs = firstPage.getParagraphsList();
//
//		      for (Document.Page.Paragraph paragraph : paragraphs) {
//		        String paragraphText = getText(paragraph.getLayout().getTextAnchor(), text);
//		        System.out.printf("Paragraph text:\n%s\n", paragraphText);
//		      }

			// Form parsing provides additional output about
			// form-formatted PDFs. You must create a form
			// processor in the Cloud Console to see full field details.
			// System.out.println("The following form key/value pairs were detected:");

			for (Document.Page.FormField field : firstPage.getFormFieldsList()) {
				String fieldName = getText(field.getFieldName().getTextAnchor(), text);
				String fieldValue = getText(field.getFieldValue().getTextAnchor(), text);

				// System.out.println("Extracted form fields pair:");
				// System.out.printf("\t%s, %s\n", fieldName, fieldValue);
				outputMap.put(fieldName, fieldValue);

			}
		}
	}

	// Extract shards from the text field
	private static String getText(Document.TextAnchor textAnchor, String text) {
		if (textAnchor.getTextSegmentsList().size() > 0) {
			int startIdx = (int) textAnchor.getTextSegments(0).getStartIndex();
			int endIdx = (int) textAnchor.getTextSegments(0).getEndIndex();
			return text.substring(startIdx, endIdx);
		}
		return "[NO TEXT]";
	}

}
