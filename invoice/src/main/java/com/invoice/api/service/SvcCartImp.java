package com.invoice.api.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.invoice.api.dto.ApiResponse;
import com.invoice.api.dto.DtoCustomer;
import com.invoice.api.dto.DtoProduct;
import com.invoice.api.entity.Cart;
import com.invoice.api.repository.RepoCart;
import com.invoice.configuration.client.CustomerClient;
import com.invoice.configuration.client.ProductClient;
import com.invoice.exception.ApiException;

@Service
public class SvcCartImp implements SvcCart {

	@Autowired
	RepoCart repo;
	
	@Autowired
	CustomerClient customerCl;
	
	//Producto
	@Autowired
	ProductClient productPl;
	
	@Override
	public List<Cart> getCart(String rfc) {
		return repo.findByRfcAndStatus(rfc,1);
	}

	@Override
	public ApiResponse addToCart(Cart cart) {
		boolean bandera = false;
		
		if(!validateCustomer(cart.getRfc()))
			throw new ApiException(HttpStatus.BAD_REQUEST, "customer does not exist");
			
		/*
		 * Requerimiento 3
		 * Validar que el GTIN exista. Si existe, asignar el stock del producto a la variable product_stock 
		 */
		
		if(!validateProduct(cart.getGtin()))
			throw new ApiException(HttpStatus.BAD_REQUEST, "product does not exist");
		
		
		Integer product_stock = productPl.getProduct(cart.getGtin()).getBody().getStock();  // cambiar el valor de cero por el stock del producto recuperado de la API Product 
		
		if(cart.getQuantity() > product_stock) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "invalid quantity");
		}  
		
		/*
		 * Requerimiento 4
		 * Validar si el producto ya había sido agregado al carrito para solo actualizar su cantidad
		 */
		//Obtebemos una lista con los elementos del carrito de esa persona
		List<Cart> list = getCart(cart.getRfc());
		//Verificamos que no este el producto 
		for (int i = 0;i<list.size();i++) {
			if (cart.getGtin().equals(list.get(i).getGtin())) {
				int suma = cart.getQuantity() + list.get(i).getQuantity();
				if(suma > product_stock) {
					throw new ApiException(HttpStatus.BAD_REQUEST, "invalid quantity");
				}
				cart.setQuantity(suma);
				repo.addQuantity(cart.getRfc(), cart.getGtin(),suma);
				bandera = true;
			}
		}
		
		/*	
		 *Si el producto esta en la lista  y no supera el stock
		 *entonces actualizamos la cantidad de ese elemento si 
		 *no se encontro el elemento en la lista entonces
		 *guardamos normalmente el articulo en el carrito
		 */
		
		if(bandera){
			return new ApiResponse("quantity updated");
		}else{
			cart.setStatus(1);
			repo.save(cart);
			return new ApiResponse("item added");
		}
		
	}

	@Override
	public ApiResponse removeFromCart(Integer cart_id) {
		if (repo.removeFromCart(cart_id) > 0)
			return new ApiResponse("item removed");
		else
			throw new ApiException(HttpStatus.BAD_REQUEST, "item cannot be removed");
	}

	@Override
	public ApiResponse clearCart(String rfc) {
		if (repo.clearCart(rfc) > 0)
			return new ApiResponse("cart removed");
		else
			throw new ApiException(HttpStatus.BAD_REQUEST, "cart cannot be removed");
	}
	
	private boolean validateCustomer(String rfc) {
		try {
			ResponseEntity<DtoCustomer> response = customerCl.getCustomer(rfc);
			if(response.getStatusCode() == HttpStatus.OK)
				return true;
			else
				return false;
		}catch(Exception e) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "unable to retrieve customer information");
		}
	}
	
	/*
	 * Metodo Que recibe una cadena "gtin" obtiene un mapeo 
	 * del producto si existe y si no arroja error
	 */
	private boolean validateProduct(String gtin) {
		try {
			ResponseEntity<DtoProduct> response = productPl.getProduct(gtin);
			if(response.getStatusCode() == HttpStatus.OK)
				return true;
			else
				return false;
		}catch(Exception e) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "unable to retrieve customer information");
		}
	}
}
