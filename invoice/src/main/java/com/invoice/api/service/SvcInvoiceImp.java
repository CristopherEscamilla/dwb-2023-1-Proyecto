package com.invoice.api.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.invoice.api.dto.ApiResponse;
import com.invoice.api.dto.DtoProduct;
import com.invoice.api.entity.Cart;
import com.invoice.api.entity.Invoice;
import com.invoice.api.entity.Item;
import com.invoice.api.repository.RepoInvoice;
import com.invoice.api.repository.RepoItem;
import com.invoice.configuration.client.ProductClient;
import com.invoice.exception.ApiException;

@Service
public class SvcInvoiceImp implements SvcInvoice {

	@Autowired
	RepoInvoice repo;
	
	@Autowired
	RepoItem repoItem;
	
	@Autowired
	SvcCart svc;
	
	@Autowired
	ProductClient productCl;

	@Override
	public List<Invoice> getInvoices(String rfc) {
		return repo.findByRfcAndStatus(rfc, 1);
	}

	@Override
	public List<Item> getInvoiceItems(Integer invoice_id) {
		return repoItem.getInvoiceItems(invoice_id);
	}

	@Override
	public ApiResponse generateInvoice(String rfc) {
		/*
		 * Requerimiento 5
		 * Implementar el método para generar una factura 
		 */
		List<Cart> listproducts = svc.getCart(rfc);
		
		if (listproducts.isEmpty()) {
			throw new ApiException(HttpStatus.NOT_FOUND, "cart has no items");
		}else {
			LocalDateTime atime = LocalDateTime.now();
			List<Item> listitems = getListClient(rfc);
			Invoice invoice = new Invoice();
			
			double total = 0;
			double taxes = 0;
			double subtotal = 0;
			
			for (int i = 0;i<listitems.size();i++) { 
				total = total + listitems.get(i).getTotal();
				taxes = taxes + listitems.get(i).getTotal();
				subtotal = subtotal + listitems.get(i).getSubtotal();
			}
			
			/*
			 * Rellenamos cada uno de los campos para la factura de 
			 * un cliente
			 */
			invoice.setRfc(rfc);
			invoice.setSubtotal(subtotal);
			invoice.setTaxes(taxes);
			invoice.setTotal(total);
			invoice.setCreated_at(atime);
			invoice.setStatus(1);
			repo.save(invoice);
			
			/*	
			 * Asignamos la lista de compras a cada cliente
			 * y registamos sus items
			 */
			for (int f = 0;f<listitems.size();f++) {
				listitems.get(f).setId_invoice(invoice.getInvoice_id());
				repoItem.save(listitems.get(f));
			}
			
			/*
			 * Vaciamos el carrito despues de que se genera una
			 * factura
			 */
			svc.clearCart(rfc);
			return new ApiResponse("invoice generated");	
		}
		
	}
	
	/*
	 * Metodo Auxiliar en la creacion de la lista de productos
	 * del cliente, regresa una lista pasado un rfc
	 */
	
	private List<Item> getListClient (String rfc) {
		List<Cart> listproducts = svc.getCart(rfc);
		List<Item> listitems = new ArrayList<Item>();
		
		for (int i = 0;i<listproducts.size();i++) {
			
			DtoProduct product = productCl.getProduct(listproducts.get(i).getGtin()).getBody();
			
			double unit_price = product.getPrice();
			double total = unit_price*listproducts.get(i).getQuantity();
			double taxes = total*.16;
			double subtotal = total - taxes;
			
			Item itm = new Item();
			
			itm.setGtin(listproducts.get(i).getGtin());
			itm.setQuantity(listproducts.get(i).getQuantity());
			itm.setUnit_price(unit_price);
			itm.setSubtotal(subtotal);
			itm.setTaxes(taxes);
			itm.setTotal(total);
			itm.setStatus(1);
			
			listitems.add(itm); 
			productCl.updateProductStock(listproducts.get(i).getGtin(), product.getStock()-listproducts.get(i).getQuantity());
		}
	}
}
