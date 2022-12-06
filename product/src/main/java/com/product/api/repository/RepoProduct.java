package com.product.api.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.product.api.entity.Product;

@Repository
public interface RepoProduct extends JpaRepository<Product, Integer> {
	

	@Query(value ="SELECT * FROM product WHERE gtin = :gtin AND status = 1", nativeQuery = true)
	Product getProductByGTIN(@Param("gtin") String gtin_code);
	
	
	@Query(value ="SELECT * FROM product WHERE gtin = :gtin AND status = 0", nativeQuery = true)
	Product getProductByGTIN0(@Param("gtin") String gtin_code);
	
	@Query(value =  "SELECT * FROM product WHERE product = :product_name", nativeQuery =  true)
	Product getProductByName(@Param("product_name") String product_name);
	
	@Query(value="SELECT * FROM product WHERE product_id = :product_id AND status = 1", nativeQuery = true)
	Product findByProductId(@Param("product_id") Integer product_id);
	
	@Query(value="SELECT * FROM product WHERE category_id = :category_id", nativeQuery = true)
	List<Product> findByCategoryId(@Param("category_id") Integer category_id);
	
	@Modifying
	@Transactional
	@Query(value ="UPDATE product SET status = 1 WHERE gtin = :gtin", nativeQuery = true)
	Integer  activateProduct (@Param("gtin") String gtin);
	
	
	@Modifying
	@Transactional
	@Query(value ="UPDATE product "
			+ "SET gtin = :gtin, "
			+ "product = :product, "
			+ "description = :description, "
			+ "price = :price, "
			+ "stock = :stock, "
			+ "status = 1 "
			+ "WHERE product_id = :product_id", nativeQuery = true)
	Integer updateProduct(
			@Param("product_id") Integer product_id,
			@Param("gtin") String gtin,
			@Param("product") String product,
			@Param("description") String description,
			@Param("price") Double price,
			@Param("stock") Integer stock
	);
   

	/*	@Modifying
		@Transactional
		@Query(value ="UPDATE product "
						+ "SET gtin = :gtin, "
							+ "product = :product, "
							+ "description = :description, "
							+ "price = :price, "
							+ "stock = :stock, "
							+ "status = 1, "
							+ "category_id = :category_id "
						+ "WHERE product_id = :product_id", nativeQuery = true)
		Integer updateProduct(
				@Param("product_id") Integer product_id,
				@Param("gtin") String gtin, 
				@Param("product") String product, 
				@Param("description") String description, 
				@Param("price") Double price, 
				@Param("stock") Integer stock,
				@Param("category_id") Integer category_id
			); */
		
		@Modifying
		@Transactional
		@Query(value ="UPDATE product SET status = 0 WHERE product_id = :product_id AND status = 1", nativeQuery = true)
		Integer deleteProduct(@Param("product_id") Integer product_id);
		
		@Modifying
		@Transactional
		@Query(value ="UPDATE product SET stock = :stock WHERE gtin = :gtin AND status = 1", nativeQuery = true)
		Integer updateProductStock(@Param("gtin") String gtin, @Param("stock") Integer stock);
	}

