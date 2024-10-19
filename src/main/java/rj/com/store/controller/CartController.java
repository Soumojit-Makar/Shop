package rj.com.store.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rj.com.store.datatransferobjects.AddItemToCartRequest;
import rj.com.store.datatransferobjects.ApiResponseMessage;
import rj.com.store.datatransferobjects.CartDTO;
import rj.com.store.services.CartService;


/**
 * Controller for handling cart-related APIs.
 *
 * This class provides endpoints for managing user carts, including adding and removing items,
 * clearing the cart, and retrieving cart details for a specific user.
 * It ensures security using role-based access control for different operations.
 */
@RestController
@RequestMapping("/carts/v1")
@SecurityRequirement(name = "scheme")
@Tag(name = "Cart Controller", description = "APIs for cart operations")
public class CartController {
    CartService cartService;
    /**
     * Constructs an instance of {@link CartController} with the specified cart service dependency.
     *
     * @param cartService the service for managing cart-related operations
     */
    public CartController(CartService cartService){
        this.cartService=cartService;
    }
    /**
     * Adds an item to the cart for a specific user.
     *
     * This endpoint accepts a user ID and the item details in the request body,
     * and adds the item to the user's cart.
     *
     * @param userId  the ID of the user to whom the item should be added
     * @param request the request object containing item details to be added to the cart
     * @return a ResponseEntity containing the updated CartDTO after adding the item
     */
    @PostMapping("/{userId}")
    @Operation(summary = "Create cart by user Id ")
    public ResponseEntity<CartDTO> addItemToCart(@PathVariable("userId") String userId ,@RequestBody AddItemToCartRequest request ){
        return new ResponseEntity<>(cartService.addItemToCart(userId,request), HttpStatus.ACCEPTED);
    }
    /**
     * Removes an item from the cart for a specific user.
     *
     * This endpoint accepts a user ID and an item ID, and removes the item from the user's cart.
     *
     * @param userId the ID of the user whose cart item should be removed
     * @param itemId the ID of the item to be removed from the cart
     * @return a ResponseEntity with a message indicating successful removal of the item
     */
    @DeleteMapping("/{userId}/items/{itemId}")
    @Operation(summary = "Removed item from cart by user and item Id")
    public ResponseEntity<ApiResponseMessage> removeItemFromCart(@PathVariable("userId") String userId ,@PathVariable("itemId") int  itemId){
        cartService.removeItemFromCart(userId,itemId);
        return new ResponseEntity<>(
                ApiResponseMessage.builder()
                        .massage("Item is removed !!")
                        .success(true)
                        .httpStatus(HttpStatus.OK)
                        .build(),
                HttpStatus.OK);
    }
    /**
     * Clears all items from the cart for a specific user.
     *
     * This endpoint accepts a user ID and clears all items in the user's cart.
     *
     * @param userId the ID of the user whose cart should be cleared
     * @return a ResponseEntity with a message indicating successful clearing of the cart
     */
    @DeleteMapping("/{userId}")
    @Operation(summary = "Clear Cart")
    public ResponseEntity<ApiResponseMessage> clearCart(@PathVariable("userId") String userId ){
        cartService.clearCart(userId);
        return new ResponseEntity<>(
                ApiResponseMessage.builder()
                        .massage("Cart is clear !!")
                        .success(true)
                        .httpStatus(HttpStatus.OK)
                        .build(),
                HttpStatus.OK);
    }
    /**
     * Retrieves the cart details for a specific user.
     *
     * This endpoint accepts a user ID and returns the details of the user's cart.
     *
     * @param userId the ID of the user whose cart details should be retrieved
     * @return a ResponseEntity containing the CartDTO for the specified user
     */
    @GetMapping("/{userId}")
    @Operation(summary = "Get cart by user Id")
    public ResponseEntity<CartDTO> getCart(@PathVariable("userId") String userId ){
        return new ResponseEntity<>(cartService.getCartByUser(userId), HttpStatus.ACCEPTED);
    }
}
