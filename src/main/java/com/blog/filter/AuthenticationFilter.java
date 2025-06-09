package com.blog.filter;

import com.blog.dao.VisitsDao;
import com.blog.dao.impl.VisitsDaoImpl;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class AuthenticationFilter implements Filter {
    private List<String> excludedUrls;
    private VisitsDao visitsDao;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String excludedUrlsStr = filterConfig.getInitParameter("excludedUrls");
        if (excludedUrlsStr != null) {
            excludedUrls = Arrays.asList(excludedUrlsStr.split(","));
        }
        visitsDao = new VisitsDaoImpl();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);

        String requestURI = req.getRequestURI();
        String contextPath = req.getContextPath();
        String path = requestURI.substring(contextPath.length());

        // 检查是否是排除的URL
        boolean isExcluded = false;
        for (String excludedUrl : excludedUrls) {
            if (path.startsWith(excludedUrl.trim())) {
                isExcluded = true;
                break;
            }
        }

        // 记录访问统计
        String userAgent = req.getHeader("User-Agent");
        if (userAgent != null) {
            visitsDao.recordVisit(userAgent);
        }

        if (isExcluded || (session != null && session.getAttribute("user") != null)) {
            chain.doFilter(request, response);
        } else {
            resp.sendRedirect(req.getContextPath() + "/login");
        }
    }

    @Override
    public void destroy() {
        // 清理资源
    }
}