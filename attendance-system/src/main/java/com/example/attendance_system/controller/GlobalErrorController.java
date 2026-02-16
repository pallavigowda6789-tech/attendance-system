package com.example.attendance_system.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class GlobalErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {

        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());

            model.addAttribute("statusCode", statusCode);

            if (statusCode == 404) {
                model.addAttribute("errorMessage", "Page not found");
                return "error/404";
            } else if (statusCode == 403) {
                model.addAttribute("errorMessage", "Access denied");
                return "error/403";
            } else if (statusCode == 500) {
                model.addAttribute("errorMessage", "Internal server error");
                return "error/500";
            }
        }

        model.addAttribute("errorMessage", "Unexpected error occurred");
        return "error/general";
    }
}