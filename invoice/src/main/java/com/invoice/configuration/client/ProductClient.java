package com.invoice.configuration.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import com.invoice.api.dto.ApiResponse;
import com.invoice.api.dto.DtoProduct;


/*
 * Especificamos al cliente de Feign la api que vamos a consumir
 * en este caso end poinst de product
 */
@FeignClient(name = "product-service")
public interface ProductClient {

	/*
	 * Requerimiento 3
	 * Actualizar método getProduct para obtener la información necesaria de un producto
	 */
	
	//Vamos a mapear un Product a un DtoProduct 
	
	@GetMapping("product/{gtin}")
    public ResponseEntity<DtoProduct> getProduct(@PathVariable("gtin") String gtin);
	
	@PutMapping("product/{gtin}/stock/{stock}")
	public ResponseEntity<ApiResponse> updateProductStock(@PathVariable("gtin") String gtin, @PathVariable("stock") Integer stock);

}
