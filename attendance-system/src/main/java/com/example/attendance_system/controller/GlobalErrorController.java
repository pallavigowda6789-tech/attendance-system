package com.example.attendance_system.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Custom error controller for handling error pages.
 * Uses a different path to avoid conflict with Spring Boot's BasicErrorController.
 */
@Controller
public class GlobalErrorController {

    private static final Logger logger = LoggerFactory.getLogger(GlobalErrorController.class);

    @RequestMapping("/app-error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);

        int statusCode = status != null ? Integer.parseInt(status.toString()) : 500;
        String errorMessage = message != null ? message.toString() : "An unexpected error occurred";

        logger.error("Error {} occurred: {}", statusCode, errorMessage);

        model.addAttribute("statusCode", statusCode);
        model.addAttribute("errorMessage", errorMessage);

        HttpStatus httpStatus = HttpStatus.resolve(statusCode);
        if (httpStatus != null) {
            model.addAttribute("statusReason", httpStatus.getReasonPhrase());
        }

        return switch (statusCode) {
            case 400 -> {
                model.addAttribute("errorMessage", "Bad Request - The server could not understand your request");
                yield "error/400";
            }
            case 401 -> {
                model.addAttribute("errorMessage", "Unauthorized - Please log in to access this resource");
                yield "error/401";
            }
            case 403 -> {
                model.addAttribute("errorMessage", "Access Denied - You don't have permission to access this resource");
                yield "error/403";
            }
            case 404 -> {
                model.addAttribute("errorMessage", "Page Not Found - The requested page could not be found");
                yield "error/404";
            }
            case 500 -> {
                model.addAttribute("errorMessage", "Internal Server Error - Something went wrong on our end");
                yield "error/500";
            }
            default -> "error/500";
        };
    }
    
    /**
     * Handle specific error codes via direct URL access.
     */
    @RequestMapping("/error/400")
    public String error400(Model model) {
        model.addAttribute("statusCode", 400);
        model.addAttribute("errorMessage", "Bad Request");
        return "error/400";
    }
    
    @RequestMapping("/error/401")
    public String error401(Model model) {
        model.addAttribute("statusCode", 401);
        model.addAttribute("errorMessage", "Unauthorized");
        return "error/401";
    }
    
    @RequestMapping("/error/403")
    public String error403(Model model) {
        model.addAttribute("statusCode", 403);
        model.addAttribute("errorMessage", "Access Denied");
        return "error/403";
    }
    
    @RequestMapping("/error/404")
    public String error404(Model model) {
        model.addAttribute("statusCode", 404);
        model.addAttribute("errorMessage", "Page Not Found");
        return "error/404";
    }
    
    @RequestMapping("/error/500")
    public String error500(Model model) {
        model.addAttribute("statusCode", 500);
        model.addAttribute("errorMessage", "Internal Server Error");
        return "error/500";
    }
}