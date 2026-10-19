package com.ptit.library.web;

import com.ptit.library.model.AuthSession;
import com.ptit.library.model.ReaderLoanItem;
import com.ptit.library.model.ReaderProfile;
import com.ptit.library.service.ReaderSelfService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReaderLoanServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!RoleGuard.require(req, resp, "READER")) {
            return;
        }
        AuthSession auth = SessionAuth.get(req.getSession(false));
        if (auth == null || auth.readerId() == null) {
            RoleGuard.sendForbidden(req, resp);
            return;
        }

        ReaderSelfService service = (ReaderSelfService) getServletContext().getAttribute(ReaderSelfService.ATTR_NAME);

        // Chống IDOR: Nếu URL cố tình truyền readerId hoặc loanId của người khác -> trả 403
        String readerIdParam = req.getParameter("readerId");
        if (readerIdParam != null && !readerIdParam.isBlank()) {
            Long requestedReaderId = parseLongObj(readerIdParam);
            if (requestedReaderId == null || !requestedReaderId.equals(auth.readerId())) {
                RoleGuard.sendForbidden(req, resp);
                return;
            }
        }

        String loanIdParam = req.getParameter("loanId");
        if (loanIdParam != null && !loanIdParam.isBlank()) {
            Long requestedLoanId = parseLongObj(loanIdParam);
            if (requestedLoanId == null || !service.verifyLoanOwnership(requestedLoanId, auth.readerId())) {
                RoleGuard.sendForbidden(req, resp);
                return;
            }
        }

        Optional<ReaderProfile> profileOpt = service.findProfileByUserId(auth.userId());
        List<ReaderLoanItem> allLoans = service.listAllLoans(auth.readerId());
        List<ReaderLoanItem> borrowedItems = new ArrayList<>();
        for (ReaderLoanItem item : allLoans) {
            if (item.isBorrowed()) {
                borrowedItems.add(item);
            }
        }

        req.setAttribute("auth", auth);
        req.setAttribute("profile", profileOpt.orElse(null));
        req.setAttribute("borrowedItems", borrowedItems);
        req.setAttribute("historyItems", allLoans);
        req.getRequestDispatcher("/WEB-INF/jsp/reader-my-loans.jsp").forward(req, resp);
    }

    private static Long parseLongObj(String value) {
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
