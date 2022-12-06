package com.product.api.service;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.product.api.dto.ApiResponse;
import com.product.api.dto.ProductDTO;
import com.product.api.entity.Category;
import com.product.api.entity.Product;
import com.product.api.repository.RepoCategory;
import com.product.api.repository.RepoProduct;
import com.product.exception.ApiException;

@Service
public class SvcProductImp implements SvcProduct {

	@Autowired
	RepoProduct repo;
	
	@Autowired
	RepoCategory repoCategory;

	@Override
	public Product getProduct(String gtin) {
		Product product =repo.getProductByGTIN(gtin);; // sustituir null por la llamada al método implementado en el repositorio
		if (product != null) {
			product.setCategory(repoCategory.findByCategoryId(product.getCategory_id()));
			return product;
		}else
			throw new ApiException(HttpStatus.NOT_FOUND, "product does not exist");
	}
	
	
	@Override
	public List<ProductDTO> listProducts(Integer category_id) {
		List<Product> lstProducts = repo.findByCategoryId(category_id);
		List<ProductDTO> lstDTOs = new ArrayList<ProductDTO>();
		
		for(Product i : lstProducts) {
			ProductDTO dto = new ProductDTO(i.getProduct_id(),i.getGtin(), i.getProduct(), i.getPrice());
			lstDTOs.add(dto);
		}
			return  lstDTOs;
	}

	/*
	 * 4. Implementar el método createProduct considerando las siguientes validaciones:
  		1. validar que la categoría del nuevo producto exista
  		2. el código GTIN y el nombre del producto son únicos
  		3. si al intentar realizar un nuevo registro ya existe un producto con el mismo GTIN pero tiene estatus 0, 
  		   entonces se debe cambiar el estatus del producto existente a 1 y actualizar sus datos con los del nuevo registro
	 */
	
	@Override
	public ApiResponse createProduct(Product in) {
		Category associated_category = repoCategory.findByCategoryId(in.getCategory_id());
		Product desactivated_product = repo.getProductByGTIN0(in.getGtin());
		Product exist_product_gtin = repo.getProductByGTIN(in.getGtin());
		Product exist_product_name = repo.getProductByName(in.getProduct());
		
		if (associated_category == null)
			throw new ApiException(HttpStatus.NOT_FOUND, "category not found");
		
		if (desactivated_product != null) {
			repo.activateProduct(in.getGtin());
			return new ApiResponse("product activated");
		}
		
		if (exist_product_gtin != null) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "product gtin already exist");
		}
		
		if (exist_product_name != null) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "product name already exist");
		}
		
		
		in.setStatus(1);
		repo.save(in);
		
		return new ApiResponse("product created");
	}

	@Override
	public ApiResponse updateProduct(Product in, Integer id) {
		Product newProduct =  (Product) repo.findByProductId(id);
		Category associated_category = repoCategory.findByCategoryId(in.getCategory_id());
		Product exist_product_gtin = repo.getProductByGTIN(in.getGtin());
		Product exist_product_name = repo.getProductByName(in.getProduct());
		
		if(newProduct == null) {
				throw new ApiException(HttpStatus.NOT_FOUND,"product does not exists");}
		else {
			if (associated_category == null) {
				throw new ApiException(HttpStatus.NOT_FOUND, "category not found");}
			else {
				if (exist_product_gtin != null) {
					throw new ApiException(HttpStatus.BAD_REQUEST, "product gtin already exist");}
				else {
					if (exist_product_name != null) {
						throw new ApiException(HttpStatus.BAD_REQUEST, "product name already exist");}
					else {repo.updateProduct(id, in.getGtin(), in.getProduct(), in.getDescription(), in.getPrice(), in.getStock());}
					}
			}
		}
	
	
		return new ApiResponse("product updated");
		
		//repo.updateProduct(id, in.getGtin(), in.getProduct(), in.getDescription(), in.getPrice(), in.getStock());
	}

	
	
	@Override
	public ApiResponse deleteProduct(Integer id) {
		if (repo.deleteProduct(id) > 0)
			return new ApiResponse("product removed");
		else
			throw new ApiException(HttpStatus.BAD_REQUEST, "product cannot be deleted");
	}

	@Override
	public ApiResponse updateProductStock(String gtin, Integer stock) {
		Product product = getProduct(gtin);
		if(stock > product.getStock())
			throw new ApiException(HttpStatus.BAD_REQUEST, "stock to update is invalid");
		
		repo.updateProductStock(gtin, product.getStock() - stock);
		return new ApiResponse("product stock updated");
	}
}
