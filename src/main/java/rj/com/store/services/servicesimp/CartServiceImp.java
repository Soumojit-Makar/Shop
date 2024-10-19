package rj.com.store.services.servicesimp;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import rj.com.store.datatransferobjects.AddItemToCartRequest;
import rj.com.store.datatransferobjects.CartDTO;
import rj.com.store.enities.Cart;
import rj.com.store.enities.CartItem;
import rj.com.store.enities.Product;
import rj.com.store.enities.User;
import rj.com.store.exceptions.BadApiRequest;
import rj.com.store.exceptions.ResourceNotFoundException;
import rj.com.store.repositories.CartItemRepository;
import rj.com.store.repositories.CartRepository;
import rj.com.store.repositories.ProductRepository;
import rj.com.store.repositories.UserRepositories;
import rj.com.store.services.CartService;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class CartServiceImp implements CartService {
    Logger logger= LoggerFactory.getLogger(CartServiceImp.class);
   private final ProductRepository productRepository;
   private static final String USER_NOT_FOUND="User not found";
   private final UserRepositories userRepositories;

    private final CartRepository cartRepository;

    private final ModelMapper mapper;

    private final CartItemRepository cartItemRepository;
    /**
     * Constructs a CartServiceImp instance with the specified dependencies.
     *
     * @param productRepository     the repository for product-related operations
     * @param userRepositories      the repository for user-related operations
     * @param cartRepository        the repository for cart-related operations
     * @param mapper                the model mapper for converting between entities and DTOs
     * @param cartItemRepository    the repository for cart item-related operations
     */

    public CartServiceImp( ProductRepository productRepository, UserRepositories userRepositories, CartRepository cartRepository, ModelMapper mapper, CartItemRepository cartItemRepository) {
        this.productRepository = productRepository;
        this.userRepositories = userRepositories;
        this.cartRepository = cartRepository;
        this.mapper = mapper;
        this.cartItemRepository = cartItemRepository;
    }
    /**
     * Adds an item to the user's cart, creating the cart if it does not exist.
     *
     * @param userId the ID of the user whose cart is being updated
     * @param request the request containing the product ID and quantity to add
     * @return the updated CartDTO containing the current state of the cart
     * @throws BadApiRequest if the requested quantity is not valid
     * @throws ResourceNotFoundException if the product or user is not found
     */
    @Override
    public CartDTO addItemToCart(String userId, AddItemToCartRequest request) {
        int quantity = request.getQuantity();
        String productId = request.getProductId();
        if (quantity <= 0) {
            throw new BadApiRequest("Requested quantity is not valid");
        }
        // Fetch the product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product is not found"));
        // Fetch the user from db
        User user = userRepositories.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        Cart cart = null;
        try {
            cart = cartRepository.findByUser(user).get();
        } catch (NoSuchElementException e) {
            cart = new Cart();
            cart.setCartId(UUID.randomUUID().toString());
            cart.setCreateAt(new Date());
            cart.setUser(user);
        }

        // Perform cart operations
        AtomicBoolean updated = new AtomicBoolean(false);
        List<CartItem> items = cart.getItems();
        items = items.stream().map(item -> {
            if (item.getProduct().getProductId().equalsIgnoreCase(productId)) {
                // Item is present
                item.setQuantity(quantity);
                item.setTotalPrice(quantity * (product.getPrice() - product.getDiscountedPrice()));
                updated.set(true);
            }
            return item;
        }).toList();

        // Create item
        if (!updated.get()) {
            CartItem cartItem = CartItem.builder()
                    .quantity(quantity)
                    .totalPrice(quantity * (product.getPrice() - product.getDiscountedPrice()))
                    .cart(cart)
                    .product(product)
                    .build();
            cart.getItems().add(cartItem);
        } else {
            cart.setItems(items);
        }

        Cart updatedCart = cartRepository.save(cart);
        return mapper.map(updatedCart, CartDTO.class);
    }
    /**
     * Removes an item from the user's cart.
     *
     * @param userId   the ID of the user whose cart is being modified
     * @param cartItem the ID of the cart item to be removed
     * @throws ResourceNotFoundException if the user, cart, or cart item is not found
     * @throws BadApiRequest if the specified cart item is not present in the user's cart
     */
    @Override
    public void removeItemFromCart(String userId, int cartItem) {
        User user = userRepositories.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        CartItem cartItem1 = cartItemRepository.findById(cartItem)
                .orElseThrow(() -> new ResourceNotFoundException("Cart Item not found"));

        if (cart.getItems().contains(cartItem1)) {
            cart.getItems().remove(cartItem1);
            cartRepository.save(cart);
            cartItemRepository.delete(cartItem1);
        } else {
            throw new BadApiRequest("This cart item is not present in this user's cart.");
        }
    }
    /**
     * Clears all items from the user's cart.
     *
     * @param userId the ID of the user whose cart is to be cleared
     * @throws ResourceNotFoundException if the user or their cart is not found
     */
    @Override
    public void clearCart(String userId) {
        User user = userRepositories.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
        logger.info("User found {}", user);

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found from this user"));
        logger.info("Cart found {}", cart);

        cart.getItems().clear();
        logger.info("Cart is removed");
        cartRepository.save(cart);
    }
    /**
     * Retrieves the cart associated with the specified user.
     *
     * @param userId the ID of the user whose cart is to be retrieved
     * @return the CartDTO object representing the user's cart
     * @throws ResourceNotFoundException if the user or their cart is not found
     */
    @Override
    public CartDTO getCartByUser(String userId) {
        User user = userRepositories.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found from this user"));
        return mapper.map(cart, CartDTO.class);
    }

}
