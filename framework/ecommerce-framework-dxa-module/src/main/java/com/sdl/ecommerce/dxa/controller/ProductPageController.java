package com.sdl.ecommerce.dxa.controller;

import com.sdl.ecommerce.api.ProductDetailResult;
import com.sdl.ecommerce.api.ProductDetailService;
import static com.sdl.ecommerce.dxa.ECommerceRequestAttributes.*;
import com.sdl.webapp.common.api.content.ContentProviderException;
import com.sdl.webapp.common.api.content.PageNotFoundException;
import com.sdl.webapp.common.api.model.MvcData;
import com.sdl.webapp.common.api.model.PageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Product Page Controller
 * Manage product detail pages. It provides E-Commerce data for current product (extracted from the URL) which the different
 * widgets can consume.
 * Provides the following SEO friendly URL format: /p/[product name]/[product ID/SKU]. Example: /p/denim-blue-skirt/49392823
 *
 * @author nic
 */
@Controller
@RequestMapping("/p")
public class ProductPageController extends AbstractECommercePageController {

    @Autowired
    private ProductDetailService detailService;

    /**
     * Handle product detail page.
     * @param request
     * @param response
     * @return view
     * @throws ContentProviderException
     */
    @RequestMapping(method = RequestMethod.GET, value = "/**", produces = {MediaType.TEXT_HTML_VALUE})
    public String handleProductDetailPage(HttpServletRequest request, HttpServletResponse response) throws ContentProviderException {

        // TODO: How to handle variants ??

        // Path pattern for product detail pages: /p/[SEO name]/[second id]
        final String requestPath = webRequestContext.getRequestPath().replaceFirst("/p", "");
        final String[] pathTokens = requestPath.split("/");
        final String productSeoId;
        final String productId;
        if ( pathTokens.length == 3 ) {
            productSeoId = pathTokens[1];
            productId = pathTokens[2];
        }
        else if ( pathTokens.length == 2 ) {
            productSeoId = null;
            productId = pathTokens[1];
        }
        else {
            throw new PageNotFoundException("Invalid product detail URL.");
        }

        //final Category category = fredhopperService.getCategoryByPath(requestPath);
        //final List<FacetParameter> facets = fredhopperService.getFacetParametersFromRequestMap(request.getParameterMap());
        ProductDetailResult detailResult = this.detailService.getDetail(productId);

        if ( detailResult != null && detailResult.getProductDetail() != null ) {

            request.setAttribute(PRODUCT_ID, productId);
            request.setAttribute(PRODUCT, detailResult.getProductDetail());
            request.setAttribute(RESULT, detailResult);
            request.setAttribute(URL_PREFIX, "/c");
            final PageModel templatePage = resolveTemplatePage(request, this.getSearchPath(productSeoId, productId));
            templatePage.setTitle(detailResult.getProductDetail().getName());

            final MvcData mvcData = templatePage.getMvcData();
            return this.viewResolver.resolveView(mvcData, "Page", request);
        }
        throw new PageNotFoundException("Product detail page not found.");
    }

    /**
     * Get search path for finding appropriate template page.
     * @param productSeoId
     * @param productId
     * @return search path
     */
    protected List<String> getSearchPath(String productSeoId, String productId) {

        // TODO: Can we extract the categories from the product???

        // Should we allow some alternative lookup mechanism here as well? Such as search for

        List<String> searchPath = new ArrayList<>();
        if ( productSeoId != null ) {
            searchPath.add("/products/" + productSeoId);

        }
        searchPath.add("/products/" + productId);
        searchPath.add("/products/generic");
        return searchPath;
    }
}