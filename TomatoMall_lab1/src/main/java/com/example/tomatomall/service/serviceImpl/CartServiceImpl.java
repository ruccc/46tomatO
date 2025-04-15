package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.TomatoException.BusinessException;
import com.example.tomatomall.TomatoException.CartNotFoundException;
import com.example.tomatomall.TomatoException.ProductNotFoundException;
import com.example.tomatomall.dto.CartResponseDTO;
import com.example.tomatomall.dto.CheckoutRequestDTO;
import com.example.tomatomall.dto.CheckoutResponseDTO;
import com.example.tomatomall.po.Cart;
import com.example.tomatomall.po.Product;
import com.example.tomatomall.repository.CartRepository;
import com.example.tomatomall.repository.OrdersRepository;
import com.example.tomatomall.repository.ProductRepository;
import com.example.tomatomall.service.CartService;
import com.example.tomatomall.util.SecurityUtil;
import com.example.tomatomall.vo.CartListVO;
import com.example.tomatomall.vo.CartVO;
import com.example.tomatomall.vo.OrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    SecurityUtil securityUtil;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final OrdersRepository ordersRepository;

    @Autowired
    public CartServiceImpl(CartRepository cartRepository, ProductRepository productRepository, OrdersRepository ordersRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.ordersRepository = ordersRepository;
    }

    @Override
    public CartVO createCartItem(CartVO cartVO) {
        Product product = productRepository.findById(cartVO.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("商品不存在"));
        if (cartVO.getQuantity() > product.getStockpile().getAmount()) {
            throw new BusinessException("库存不足");
        }
        Cart cart = new Cart();
        cart.setUserId(getCurrentUserId());
        cart.setProductId(cartVO.getProductId());
        cart.setQuantity(cartVO.getQuantity());
        Cart savedCart = cartRepository.save(cart);
        CartVO result = new CartVO();
        result.setCartItemId(savedCart.getCartItemId());
        result.setUserId(savedCart.getUserId());
        result.setProductId(savedCart.getProductId());
        result.setQuantity(savedCart.getQuantity());
        result.setTitle(product.getTitle());
        result.setPrice(product.getPrice());
        result.setDescription(product.getDescription());
        result.setCover(product.getCover());
        result.setDetail(product.getDetail());
        return result;
    }

    @Override
    public void deleteCartItem(String cartItemId) {
        Cart cart = cartRepository.findById(cartItemId)
                .orElseThrow(CartNotFoundException::new);
        cartRepository.delete(cart);
    }

    @Override
    public void adjustCartQuantity(String cartItemId, int quantity) {
        Cart cart = cartRepository.findById(cartItemId)
                .orElseThrow(CartNotFoundException::new);
        Product product = productRepository.findById(cart.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("商品不存在"));
        if (quantity > product.getStockpile().getAmount()) {
            throw new BusinessException("库存不足");
        }
        cart.setQuantity(quantity);
        cartRepository.save(cart);
    }

    @Override
    public CheckoutResponseDTO generateOrder(CheckoutRequestDTO checkoutRequestDTO) {
        String username = securityUtil.getCurrentAccount().getUsername();
        int userId = securityUtil.getCurrentAccount().getId();
        BigDecimal totalAmount = BigDecimal.ZERO;
        String productId = null;
        for(String s :checkoutRequestDTO.cartItemIds){
            productId=cartRepository.findByCartItemId(s).getProductId();
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ProductNotFoundException("商品不存在"));
            totalAmount =totalAmount.add(product.getPrice());
        }
        OrderVO orderVO = new OrderVO();
        orderVO.setUserId(userId);
        orderVO.setTotalAmount(totalAmount);
        orderVO.setPaymentMethod("AliPay");
        orderVO.setStatus("PENDING");
        orderVO.setCreateTime(LocalDateTime.now());
        ordersRepository.save(orderVO.toPO());
        CheckoutResponseDTO crDTO = new CheckoutResponseDTO();
        crDTO.setOrderId(orderVO.getOrderId());
        crDTO.setUsername(username);
        crDTO.setTotalAmount(totalAmount);
        crDTO.setPaymentMethod("ALIPAY");
        crDTO.setCreateTime(orderVO.getCreateTime());
        crDTO.setStatus(orderVO.getStatus());
        return crDTO;
    }

    @Override
    public CartListVO getCart() {
        Integer userId = getCurrentUserId();
        List<Cart> carts = cartRepository.findByUserId(userId);

        List<CartVO> items = carts.stream().map(cart -> {
            Product product = productRepository.findById(cart.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException("商品不存在"));

            CartVO vo = new CartVO();
            vo.setCartItemId(cart.getCartItemId());
            vo.setUserId(cart.getUserId());
            vo.setProductId(cart.getProductId());
            vo.setQuantity(cart.getQuantity());
            vo.setTitle(product.getTitle());
            vo.setPrice(product.getPrice());
            vo.setDescription(product.getDescription());
            vo.setCover(product.getCover());
            vo.setDetail(product.getDetail());
            return vo;
        }).collect(Collectors.toList());

        List<CartResponseDTO> responseItems = items.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        CartListVO result = new CartListVO();
        result.setItems(responseItems);
        result.setTotal(items.size());
        result.setTotalAmount(calculateTotalAmount(items));
        return result;
    }

    private CartResponseDTO convertToResponseDTO(CartVO vo) {
        CartResponseDTO dto = new CartResponseDTO();
        dto.setCartItemId(vo.getCartItemId());
        dto.setProductId(vo.getProductId());
        dto.setQuantity(vo.getQuantity());
        dto.setTitle(vo.getTitle());
        dto.setPrice(vo.getPrice());
        dto.setDescription(vo.getDescription());
        dto.setCover(vo.getCover());
        dto.setDetail(vo.getDetail());
        return dto;
    }

    private BigDecimal calculateTotalAmount(List<CartVO> items) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartVO item : items) {
            totalAmount = BigDecimal.valueOf(item.getQuantity()).multiply(item.getPrice()).add(totalAmount);
        }
        return totalAmount;
    }

    private Integer getCurrentUserId() {
        return securityUtil.getCurrentAccount().getId();
    }
}