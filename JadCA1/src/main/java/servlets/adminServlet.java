package servlets;

import java.io.File;
import java.io.IOException;
import java.io.*;
import java.sql.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.text.SimpleDateFormat;
import models.*;
import services.BookServices;
import services.CategoryServices;
import config.*;
import com.cloudinary.*;
import com.cloudinary.utils.ObjectUtils;
/**
 * Servlet implementation class adminServlet
 */
@WebServlet("/admin")
@MultipartConfig(
		location = "/tmp",
	    fileSizeThreshold = 1024 * 1024, // 1MB
	    maxFileSize = 1024 * 1024 * 10, // 15MB
	    maxRequestSize = 1024 * 1024 * 50 // 50MB
	)
public class adminServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public adminServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
//		response.getWriter().append("Served at: ").append(request.getContextPath());
		HttpSession session = request.getSession();
		//String userRole = (String) session.getAttribute("userRole")s;
		String userRole = "admin";
//		if(userRole == null ) {
//			response.sendRedirect("login.jsp");
//			return;
//		}
		// Get the servlet configuration
	    ServletConfig servletConfig = getServletConfig();

	    // Retrieve the servlet version
	    String servletVersion = servletConfig.getServletContext().getMajorVersion() + "." + servletConfig.getServletContext().getMinorVersion();

	    // Print the servlet versions
	    System.out.println("Servlet version: " + servletVersion);
		if(userRole.equals("admin")) {
			RequestDispatcher dispatcher;
			String pageNumberStr = request.getParameter("pageNumber");
			String recordsPerPageStr = request.getParameter("recordPerPage");
			int pageNumber,recordsPerPage;
			if(pageNumberStr == null || recordsPerPageStr == null ) {
				pageNumber = 3;
				recordsPerPage = 6;
			}
			else {
				pageNumber = Integer.parseInt(pageNumberStr);
				recordsPerPage = Integer.parseInt(recordsPerPageStr);
			}
			List<Book> bookDataResults = BookServices.fetchBookData(pageNumber,recordsPerPage);
			List<Category> categoryDataResult = CategoryServices.getAllCategory();
			request.setAttribute("bookResults", bookDataResults);
			request.setAttribute("categoryResults", categoryDataResult);
			dispatcher = request.getRequestDispatcher("adminDashboard.jsp");
			dispatcher.forward(request, response);
		}
		else {
			response.sendRedirect("login.jsp");
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		HttpSession session = request.getSession();
		System.out.println("Admin Servlet is called");
		//String userRole = (String) session.getAttribute("userRole");
		String userRole = "admin";
		if(userRole.equals("admin")) {
			RequestDispatcher dispatcher;
			String action = getValue(request.getPart("action"));
			System.out.println(action);
			if(action!=null) {
				switch(action) {
				case "addBook":
					try {
						String title = getValue(request.getPart("title"));
						String author = getValue(request.getPart("author"));
						double price = Double.parseDouble(getValue(request.getPart("price")));
						String publisher = getValue(request.getPart("publisher"));
						String dateString = getValue(request.getPart("pubDate"));
						SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
						java.util.Date utilPubDate = dateFormat.parse(dateString);
						java.sql.Date pubDate = new java.sql.Date(utilPubDate.getTime());
						String ISBN = getValue(request.getPart("ISBN"));
						float rating = Float.parseFloat(getValue(request.getPart("rating")));
						String description = getValue(request.getPart("description"));
						int categoryID = Integer.parseInt(getValue(request.getPart("category")));
						int quantity = Integer.parseInt(getValue(request.getPart("quantity")));
						Part imagePart = request.getPart("image");
						InputStream inputStream = imagePart.getInputStream();
						File tempFile = File.createTempFile("temp", ".jpg");
						try (OutputStream outputStream = new FileOutputStream(tempFile)) {
						    byte[] buffer = new byte[4096];
						    int bytesRead;
						    while ((bytesRead = inputStream.read(buffer)) != -1) {
						        outputStream.write(buffer, 0, bytesRead);
						    }
						}
						Cloudinary cloudinary = CloudinaryConfig.getCloudinaryInstance();
						@SuppressWarnings("unchecked")
						Map<String, Object> uploadResult = cloudinary.uploader().upload(tempFile, ObjectUtils.emptyMap());
						String imageUrl = (String) uploadResult.get("public_id");
						tempFile.delete();
						System.out.println(imageUrl);
						Book newBook = new Book(title,author,price,publisher,pubDate,ISBN,rating,description,imageUrl,categoryID,quantity); 
						String message = BookServices.addBook(newBook);
						request.setAttribute("message", message);
						doGet(request,response);
					}
					catch(Exception e) {
						e.printStackTrace();
						request.setAttribute("message", e);
						dispatcher = request.getRequestDispatcher("addBook.jsp");
						dispatcher.forward(request, response);
					}
					break;
				case "updateBook": 
					try {
						String title = request.getParameter("title");
						String author = request.getParameter("author");
						double price = Double.parseDouble(request.getParameter("price"));
						String publisher = request.getParameter("publisher");
						String dateString = request.getParameter("pubDate");
						SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
						Date pubDate = (Date) dateFormat.parse(dateString);
						String ISBN = request.getParameter("ISBN");
						float rating = Float.parseFloat(request.getParameter("rating"));
						String description = request.getParameter("description");
						int categoryID = Integer.parseInt(request.getParameter("category"));
						int quantity = Integer.parseInt(request.getParameter("quantity"));
						Part imagePart = request.getPart("image");
						Cloudinary cloudinary = CloudinaryConfig.getCloudinaryInstance();
						@SuppressWarnings("unchecked")
						Map<String, Object> uploadResult = cloudinary.uploader().upload(imagePart.getInputStream(), ObjectUtils.emptyMap());
						String imageUrl = (String) uploadResult.get("url");
						Book updateBook = new Book(title,author,price,publisher,pubDate,ISBN,rating,description,imageUrl,categoryID,quantity); 
						String message = BookServices.updateBook(updateBook);
						request.setAttribute("message", message);
						doGet(request,response);
					}
					catch(Exception e) {
						e.printStackTrace();
						request.setAttribute("message", e);
						dispatcher = request.getRequestDispatcher("updateBook.jsp");
						dispatcher.forward(request, response);
					}
					break;
				case "deleteBook":
					try {
						int bookID = Integer.parseInt(request.getParameter("bookID"));
						String imageUrl = request.getParameter("imageUrl");
						String message = BookServices.deleteBook(bookID,imageUrl);
						request.setAttribute("message", message);
						doGet(request,response);
					}catch (Exception e){
						e.printStackTrace();
						request.setAttribute("message", e);
						doGet(request,response);
					}
					break;
				case "addCategory":
					break;
				case "updateCategory":
					break;
				case "deleteCategory":
					break;
				case "addUser":
					break;
				case "updateUser":
					break;
				case "deleteUser":
					break;
				default:
					String errorMessage = "Invalid action specified.";
				    request.setAttribute("message", errorMessage);
				    doGet(request,response);
				    break;
				}
			}
			else {
				String errorMessage = "Invalid action specified.";
			    request.setAttribute("message", errorMessage);
			    doGet(request,response);
			}
		}else {
			response.sendRedirect("login.jsp");
		}
	}

	private String getValue(Part part) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(part.getInputStream(), "UTF-8"));
		StringBuilder value = new StringBuilder();
		char[] buffer = new char[1024];
		for (int length = 0; (length = reader.read(buffer)) > 0;) {
			value.append(buffer, 0, length);
		}
		return value.toString();
	}
}
