package com.sdl.ecommerce.fredhopper;

import com.fredhopper.webservice.client.Filter;
import com.fredhopper.webservice.client.Page;
import com.fredhopper.webservice.client.Universe;
import com.sdl.ecommerce.api.ProductDetailResult;
import com.sdl.ecommerce.api.model.*;
import com.sdl.ecommerce.fredhopper.model.FredhopperBreadcrumb;
import com.sdl.ecommerce.fredhopper.model.FredhopperProduct;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Fredhopper Detail Result
 *
 * @author nic
 */
public class FredhopperDetailResult extends FredhopperResultBase implements ProductDetailResult {

    private Product productDetail = null;

    public FredhopperDetailResult(Page fredhopperPage, FredhopperLinkManager linkManager) {
        super(fredhopperPage, linkManager);
    }

    @Override
    public Product getProductDetail() {
        if ( this.productDetail == null ) {
            synchronized (this) {
                if (this.productDetail == null) {
                    this.productDetail = this.getProductDetail(this.universe);
                    // TODO: Throw a NOT_FOUND exception here!! Or have some kind of status code here???
                }
            }
        }
        return this.productDetail;
    }

    @Override
    public List<Breadcrumb> getBreadcrumbs(String urlPrefix, String rootTitle) {
        return this.getProductBreadcrumbs(this.getProductDetail(), urlPrefix, rootTitle);
    }

    @Override
    public List<Promotion> getPromotions() {
        return this.getPromotions(this.universe);
    }

    private Product getProductDetail(Universe universe) {
        List<Product> products = this.getProducts(universe.getItemsSection().getItems().getItem());
        if ( products.size() > 0 ) {
            FredhopperProduct product = (FredhopperProduct) products.get(0);
            List<String> categoryIds = (List<String>) product.getAttribute("categoryId");
            if ( categoryIds != null ) {
                for ( String categoryId : categoryIds ) {
                    product.getCategories().add(this.categoryService.getCategoryById(categoryId));
                }
            }
            product.setFacets(new ArrayList<FacetParameter>());

            List<Filter> facetFilters = this.getFacetFilters(universe);
            for ( Filter filter : facetFilters ) {
                Object facetValue = product.getAttribute(filter.getOn());
                if ( facetValue != null ) {
                    FacetParameter facetParameter;
                    if ( facetValue instanceof List ) {
                        List<String> facetValues = (List<String>) facetValue;
                        facetParameter = new FacetParameter(filter.getOn());
                        facetParameter.getValues().addAll(facetValues);
                    }
                    else {
                        facetParameter = new FacetParameter(filter.getOn(), facetValue.toString());
                    }
                    product.getFacets().add(facetParameter);
                }
            }
            return product;
        }
        return null;
    }

    private List<Breadcrumb> getProductBreadcrumbs(Product product, String urlPrefix, String rootTitle) {
        List<Breadcrumb> breadcrumbs = new ArrayList<>();

        Category category = product.getCategories().get(0);
        while ( category != null ) {
            breadcrumbs.add(0, new FredhopperBreadcrumb(category.getName(), category.getCategoryLink(urlPrefix), true));
            category = category.getParent();
        }
        if ( rootTitle != null ) {
            breadcrumbs.add(0, new FredhopperBreadcrumb(rootTitle, urlPrefix, true));
        }

        return breadcrumbs;
    }

}
