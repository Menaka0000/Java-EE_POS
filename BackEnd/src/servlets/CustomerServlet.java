package servlets;

import bo.BoFactory;
import bo.custom.CustomerBo;
import dto.CustomerDTO;
import javax.annotation.Resource;
import javax.json.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.List;

@WebServlet(urlPatterns = "/customer")
public class CustomerServlet extends HttpServlet {

    private final CustomerBo customerBO = (CustomerBo) BoFactory.getBoFactory().getBO(BoFactory.BoTypes.CUSTOMER);
    @Resource(name = "java:comp/env/jdbc/pool")
    DataSource ds;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        JsonObjectBuilder ob1 = Json.createObjectBuilder();
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        try {
            final Connection connection = ds.getConnection();
            List<CustomerDTO> allCustomers = customerBO.getAllCustomers(connection);
            connection.close();
            for (CustomerDTO c : allCustomers) {
                JsonObjectBuilder ob2 = Json.createObjectBuilder();
                ob2.add("id", c.getId());
                ob2.add("name", c.getName());
                ob2.add("address", c.getAddress());
                ob2.add("contact", c.getContact());
                arrayBuilder.add(ob2.build());
            }
            ob1.add("data", arrayBuilder.build());
            ob1.add("message", "success");
            ob1.add("status", 200);
            resp.setContentType("application/json");
            PrintWriter writer = resp.getWriter();
            writer.print(ob1.build());
            System.out.println("hello happy people");

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String cusId = req.getParameter("cusId");
        String cusName = req.getParameter("cusName");
        String cusAddress = req.getParameter("cusAddress");
        String cusContact = req.getParameter("cusContact");
        try {
            final Connection connection = ds.getConnection();
            boolean b = customerBO.saveCustomer(new CustomerDTO(cusId, cusName, cusAddress, cusContact), connection);
            connection.close();
            PrintWriter writer = resp.getWriter();
            writer.write(" " + b);

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }


    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonReader reader = Json.createReader(req.getReader());
        JsonObject jOb = reader.readObject();
        JsonObjectBuilder oBuilder = Json.createObjectBuilder();
        try {
            final Connection connection = ds.getConnection();
            boolean b = customerBO.updateCustomer(new CustomerDTO(jOb.getString("id"), jOb.getString("name"), jOb.getString("address"), jOb.getString("contact")), connection);
            System.out.println(b);
            if (b) {
                oBuilder.add("message", "Customer has been updated");
                oBuilder.add("status", 200);
            } else {
                oBuilder.add("message", "Customer hasn't been updated");
                oBuilder.add("status", 500);
            }
            connection.close();
        } catch (SQLException | ClassNotFoundException throwables) {
            throwables.printStackTrace();
            oBuilder.add("message", "Customer hasn't been updated");
            oBuilder.add("status", 500);
        }
        resp.getWriter().print(oBuilder.build());
        resp.setContentType("application/json");
        resp.setStatus(200);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
        try {
            final Connection connection = ds.getConnection();
            if (customerBO.deleteCustomer( req.getParameter("cusId"),connection)){
                objectBuilder.add("message", "Customer has been deleted");
                objectBuilder.add("status", 200);
            }else {
                objectBuilder.add("message", "Customer hasn't been deleted");
                objectBuilder.add("status", 500);
            }
            connection.close();
        } catch (SQLException | ClassNotFoundException throwables) {
            throwables.printStackTrace();
            objectBuilder.add("message", "Customer hasn't been deleted");
            objectBuilder.add("status", 500);
        }
        resp.getWriter().print(objectBuilder.build());
        resp.setContentType("application/json");
        resp.setStatus(200);

    }
    /*   @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.addHeader("Access-Control-Allow-Origin", "*");
        resp.addHeader("Access-Control-Allow-Methods", "DELETE, PUT");
        resp.addHeader("Access-Control-Allow-Headers", "Content-Type");
    }*/
}