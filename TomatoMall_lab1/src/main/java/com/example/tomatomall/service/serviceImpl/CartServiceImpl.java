package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.TomatoException.BusinessException;
import com.example.tomatomall.TomatoException.CartNotFoundException;
import com.example.tomatomall.TomatoException.ProductNotFoundException;
import com.example.tomatomall.dto.CartResponseDTO;
import com.example.tomatomall.dto.CheckoutRequestDTO;
import com.example.tomatomall.dto.CheckoutResponseDTO;
import com.example.tomatomall.po.Cart;
import com.example.tomatomall.po.CartsOrdersRelation;
import com.example.tomatomall.po.Order;
import com.example.tomatomall.po.Product;
import com.example.tomatomall.repository.CartRepository;
import com.example.tomatomall.repository.CORRepository;
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
    private final CORRepository corRepository;

    @Autowired
    public CartServiceImpl(CartRepository cartRepository, ProductRepository productRepository, OrdersRepository ordersRepository, CORRepository corRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.ordersRepository = ordersRepository;
        this.corRepository = corRepository;
    }

    @Override
    public CartResponseDTO createCartItem(CartVO cartVO) {
        Product product = productRepository.findById(cartVO.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("商品不存在"));
        
        // 检查库存是否足够
        if (cartVO.getQuantity() > product.getStockpile().getAmount()) {
            throw new BusinessException("库存不足");
        }
        
        // 获取当前用户ID
        Integer userId = getCurrentUserId();
        
        // 查询用户购物车中是否已经存在相同商品
        List<Cart> userCarts = cartRepository.findByUserId(userId);
        Cart existingCart = null;
        boolean isExistingItem = false;
        
        for (Cart cart : userCarts) {
            if (cart.getProductId().equals(cartVO.getProductId())) {
                existingCart = cart;
                isExistingItem = true;
                break;
            }
        }
        
        Cart savedCart;
        
        // 如果已存在相同商品，则更新数量
        if (existingCart != null) {
            int newQuantity = existingCart.getQuantity() + cartVO.getQuantity();
            
            // 再次检查合并后的数量是否超过库存
            if (newQuantity > product.getStockpile().getAmount()) {
                throw new BusinessException("库存不足，当前购物车已有" + existingCart.getQuantity() + "件该商品");
            }
            
            existingCart.setQuantity(newQuantity);
            savedCart = cartRepository.save(existingCart);
        } else {
            // 如果不存在相同商品，则创建新的购物车项
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setProductId(cartVO.getProductId());
            cart.setQuantity(cartVO.getQuantity());
            savedCart = cartRepository.save(cart);
        }
        
        // 构建返回结果
        CartResponseDTO result = new CartResponseDTO();
        result.setCartItemId(savedCart.getCartItemId());
        result.setProductId(savedCart.getProductId());
        result.setQuantity(savedCart.getQuantity());
        result.setTitle(product.getTitle());
        result.setPrice(product.getPrice());
        result.setDescription(product.getDescription());
        result.setCover(product.getCover());
        result.setDetail(product.getDetail());
        result.setExistingItem(isExistingItem);
        
        return result;
    }

    @Override
    public void deleteCartItem(String cartItemId) {
        Cart cart = cartRepository.findById(cartItemId)
                .orElseThrow(CartNotFoundException::new);
        
        // 先查找并删除关联表中的记录
        List<CartsOrdersRelation> relations = corRepository.findByCartItemId(cartItemId);
        if (relations != null && !relations.isEmpty()) {
            for (CartsOrdersRelation relation : relations) {
                corRepository.delete(relation);
            }
        }
        
        // 然后删除购物车商品
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
        if (checkoutRequestDTO == null) {
            throw new BusinessException("结账请求不能为空");
        }

        if (checkoutRequestDTO.cartItemIds == null || checkoutRequestDTO.cartItemIds.isEmpty()) {
            throw new BusinessException("购物车列表不能为空");
        }

        String username = securityUtil.getCurrentAccount().getUsername();
        int userId = securityUtil.getCurrentAccount().getId();
        BigDecimal totalAmount = BigDecimal.ZERO;

        // 创建订单对象
        OrderVO orderVO = new OrderVO();
        orderVO.setUserId(userId);
        orderVO.setTotalAmount(BigDecimal.ZERO); // 先设为0，后面计算
        orderVO.setPaymentMethod(checkoutRequestDTO.payment_method != null ? checkoutRequestDTO.payment_method : "AliPay");
        orderVO.setStatus("PENDING");
        orderVO.setCreateTime(LocalDateTime.now());
        
        // 转换为PO并保存
        Order savedOrder = ordersRepository.save(orderVO.toPO());
        // 将生成的ID设置回OrderVO
        orderVO.setOrderId(savedOrder.getOrderId());
        
        // 保存要处理的购物车项目列表
        List<Cart> cartsToDelete = new java.util.ArrayList<>();
        
        // 保存订单后再计算总金额
        for (String cartItemId : checkoutRequestDTO.cartItemIds) {
            Cart cart = cartRepository.findByCartItemId(cartItemId);
            if (cart == null) {
                throw new CartNotFoundException();
            }

            String productId = cart.getProductId();
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ProductNotFoundException("商品不存在"));

            // 计算总金额时考虑购物车中的商品数量
            totalAmount = totalAmount.add(product.getPrice().multiply(new BigDecimal(cart.getQuantity())));
            
            // 创建购物车项和订单的关联
            CartsOrdersRelation relation = new CartsOrdersRelation();
            relation.setCartItemId(cartItemId);
            relation.setOrderId(savedOrder.getOrderId()); // 使用保存后返回的对象的ID
            corRepository.save(relation);
            
            // 将购物车项目添加到待删除列表
            cartsToDelete.add(cart);
        }
        
        // 更新订单总金额
        orderVO.setTotalAmount(totalAmount);
        savedOrder.setTotalAmount(totalAmount);
        ordersRepository.save(savedOrder);
        
        // 从关联表中删除所有关联记录，然后再删除购物车项目
        for (Cart cart : cartsToDelete) {
            List<CartsOrdersRelation> relations = corRepository.findByCartItemId(cart.getCartItemId());
            if (relations != null && !relations.isEmpty()) {
                for (CartsOrdersRelation relation : relations) {
                    corRepository.delete(relation);
                }
            }
            cartRepository.delete(cart);
        }

        CheckoutResponseDTO crDTO = new CheckoutResponseDTO();
        crDTO.setOrderId(savedOrder.getOrderId());
        crDTO.setUsername(username);
        crDTO.setTotalAmount(totalAmount);
        crDTO.setPaymentMethod(orderVO.getPaymentMethod().toUpperCase());
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
        dto.setExistingItem(false); // 默认为false，因为这是从购物车列表获取的项
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