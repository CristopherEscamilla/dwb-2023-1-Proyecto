package com.product.api.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.product.api.dto.ApiResponse;
import com.product.api.entity.Category;
import com.product.api.repository.RepoCategory;
import com.product.exception.ApiException;

@Service
public class SvcCategoryImp implements SvcCategory {

	@Autowired
	RepoCategory cat;
	
	@Override
	public List<Category> getCategories() {
		return cat.findByStatus(1);
	}

	@Override
	public Category readCategory(Integer category_id) {
		Category categorySaved = (Category) cat.findByCategoryId(category_id);
		 if(categorySaved == null) { 
			throw new ApiException(HttpStatus.BAD_REQUEST,"category does not exist");
		}else
			return categorySaved;
	}

	@Override
	public ApiResponse createCategory(Category category) {
		Category categorySaved = (Category) cat.findByCategory(category.getCategory());
		if(categorySaved != null) {
			if(categorySaved.getStatus() == 0) {
				cat.activateCategory(categorySaved.getCategory_id());
				return new ApiResponse("category has been activated");
			}else {
				throw new ApiException (HttpStatus.BAD_REQUEST,"category alredy exists");
			}
		}
		cat.createCategory(category.getCategory());
		return new ApiResponse("category created");
	}

	@Override
	public ApiResponse updateCategory(Integer category_id, Category category) {
		Category categorySaved = (Category) cat.findByCategoryId(category_id);
		if(categorySaved == null) 
			throw new ApiException(HttpStatus.BAD_REQUEST,"category does not exist");
		else {
			if(categorySaved.getStatus() == 0) {
				throw new ApiException(HttpStatus.BAD_REQUEST,"category is not active");
			}else {
				categorySaved = (Category) cat.findByCategory(category.getCategory());
				if(categorySaved != null){
					throw new ApiException(HttpStatus.BAD_REQUEST, "category already exist");
				}
				cat.updateCategory(category_id,category.getCategory());
				return new ApiResponse("category updated");
			}
		}
	}

	@Override
	public ApiResponse deleteCategory(Integer category_id) {
		Category categorySaved = (Category) cat.findByCategoryId(category_id);
		if(categorySaved == null) { 
			throw new ApiException(HttpStatus.NOT_FOUND,"category does not exist"); 
		}else {
			cat.deleteById(category_id);
			return new ApiResponse("category removed"); 
			}
	}
}
