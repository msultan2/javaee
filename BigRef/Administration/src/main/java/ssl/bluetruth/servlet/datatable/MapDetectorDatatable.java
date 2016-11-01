/*
 * THIS SOFTWARE IS PROVIDED BY SIMULATION SYSTEMS LTD ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL SIMULATION
 * SYSTEMS LTD BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2002 (C) Simulation Systems Ltd. All Rights Reserved.
 *
 * MapDetectorDatatable.java
 * @author svenkataramanappa
 *
 * Product:
 *
 * Change History: Created on August 13, 2007, 11:31 AM Version 001
 * 2015-02-27 SCJS 597/030 - Display only active icons on Map
 *
 */
package ssl.bluetruth.servlet.datatable;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import ssl.bluetruth.database.DatabaseManager;
import com.ssl.utils.datatable.DataTableResponse;
import ssl.bluetruth.utils.JsonResponseProcessor;
import com.ssl.utils.datatable.DatatableProcessor;
import com.ssl.utils.datatable.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class MapDetectorDatatable extends HttpServlet {

    private static final Logger LOGGER = LogManager.getLogger(MapDetectorDatatable.class);
    private Tuple<QueryBody, QueryColumns> queryTuple;
    private final String queryType = "detector";
    private final JsonResponseProcessor responseProcessor = new JsonResponseProcessor();

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        request.getParameterMap();

        Map<String, List<String>> params = StringUtils.convertRequestParams(request.getParameterMap());

        try {
            DatabaseManager dm = DatabaseManager.getInstance();
            DatatableProcessor dp = new DatatableProcessor(dm.getDatasource(), String.format(queryTuple.getBody().get(), request.getUserPrincipal().getName()), queryTuple.getColumns().get(), params);
            DataTableResponse responseObject = dp.processRequest();
            responseProcessor.createResponse(responseObject, response.getWriter());
        } catch (Exception ex) {
            response.setStatus(500);
            LOGGER.warn("Could not process request", ex);
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        queryTuple = new Query(queryType).construct();
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
