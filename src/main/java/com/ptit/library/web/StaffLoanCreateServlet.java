package com.ptit.library.web;

import com.ptit.library.model.AuthSession;
import com.ptit.library.service.LoanService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

// UI-12: lập phiếu mượn
public class StaffLoanCreateServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!RoleGuard.require(req, resp, "ADMIN", "LIBRARIAN")) {
            return;
        }
        req.setAttribute("auth", SessionAuth.get(req.getSession(false)));
        Flash.consume(req);
        req.getRequestDispatcher("/WEB-INF/jsp/staff-loan-create.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!RoleGuard.require(req, resp, "ADMIN", "LIBRARIAN")) {
            return;
        }
        AuthSession auth = SessionAuth.get(req.getSession(false));
        LoanService service = (LoanService) getServletContext().getAttribute(LoanService.ATTR_NAME);
        String readerCode = req.getParameter("readerCode");
        String copiesText = req.getParameter("copyCodes");
        // mỗi mã bản sách cách nhau bởi dấu phẩy hoặc xuống dòng
        List<String> copyCodes = Arrays.asList((copiesText == null ? "" : copiesText).split("[,\\s]+"));

        String result = service.createLoan(readerCode, copyCodes, auth.userId());
        if (result.startsWith("OK:")) {
            Flash.set(req, null, "Đã lập phiếu mượn " + result.substring(3) + ".");
            resp.sendRedirect(req.getContextPath() + "/staff/loans?q=" + result.substring(3));
            return;
        }
        // lỗi thì giữ lại dữ liệu đã nhập để người dùng sửa
        req.setAttribute("auth", auth);
        req.setAttribute("error", result.substring(4));
        req.setAttribute("readerCode", readerCode);
        req.setAttribute("copyCodes", copiesText);
        req.getRequestDispatcher("/WEB-INF/jsp/staff-loan-create.jsp").forward(req, resp);
    }
}
