package com.dappunq.controller;

import com.dappunq.dto.order.CrearOrdenRequest;
import com.dappunq.dto.order.OrdenPendienteResponse;
import com.dappunq.dto.order.OrdenResponse;
import com.dappunq.dto.order.TransaccionEjecutadaResponse;
import com.dappunq.model.order.Orden;
import com.dappunq.model.order.exception.CodigoErrorOrden;
import com.dappunq.model.order.exception.OrdenException;
import com.dappunq.service.order.CancelarOrdenService;
import com.dappunq.service.order.ConsultarOrdenesService;
import com.dappunq.service.order.CrearOrdenCompraService;
import com.dappunq.service.order.CrearOrdenVentaService;
import com.dappunq.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class OrderController {
    private final CrearOrdenCompraService buyOrders;
    private final CrearOrdenVentaService sellOrders;
    private final ConsultarOrdenesService queries;
    private final CancelarOrdenService cancellation;

    public OrderController(CrearOrdenCompraService buyOrders,
                           CrearOrdenVentaService sellOrders,
                           ConsultarOrdenesService queries,
                           CancelarOrdenService cancellation) {
        this.buyOrders = buyOrders;
        this.sellOrders = sellOrders;
        this.queries = queries;
        this.cancellation = cancellation;
    }

    @PostMapping("/orders/buy")
    public ResponseEntity<OrdenResponse> buy(@AuthenticationPrincipal UserPrincipal principal,
                                              @Valid @RequestBody CrearOrdenRequest request) {
        validateOwner(principal, request);
        return created(buyOrders.crear(request));
    }

    @PostMapping("/orders/sell")
    public ResponseEntity<OrdenResponse> sell(@AuthenticationPrincipal UserPrincipal principal,
                                               @Valid @RequestBody CrearOrdenRequest request) {
        validateOwner(principal, request);
        return created(sellOrders.crear(request));
    }

    private void validateOwner(UserPrincipal principal, CrearOrdenRequest request) {
        if (!principal.getUserId().equals(request.usuarioId())) {
            throw new OrdenException(CodigoErrorOrden.PROPIETARIO_INVALIDO,
                    "La orden no pertenece al usuario autenticado");
        }
    }

    @GetMapping("/users/{usuarioId}/orders")
    public List<OrdenPendienteResponse> pending(@PathVariable Long usuarioId) {
        return queries.pendientes(usuarioId).stream().map(OrdenPendienteResponse::from).toList();
    }

    @GetMapping("/users/{usuarioId}/transactions")
    public List<TransaccionEjecutadaResponse> transactions(@PathVariable Long usuarioId) {
        return queries.ejecutadas(usuarioId).stream().map(TransaccionEjecutadaResponse::from).toList();
    }

    @DeleteMapping("/orders/{orderId}")
    public ResponseEntity<Void> cancel(@PathVariable Long orderId, @RequestParam Long usuarioId) {
        cancellation.cancelar(orderId, usuarioId);
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<OrdenResponse> created(Orden orden) {
        return ResponseEntity.status(HttpStatus.CREATED).body(OrdenResponse.from(orden));
    }
}
