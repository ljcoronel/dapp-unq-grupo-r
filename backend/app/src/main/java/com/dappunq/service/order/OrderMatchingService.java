package com.dappunq.service.order;

import com.dappunq.model.order.Orden;
import com.dappunq.persistence.order.OrdenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderMatchingService {
    private final OrdenRepository orders;
    private final OrderSettlementService settlement;

    public OrderMatchingService(OrdenRepository orders, OrderSettlementService settlement) {
        this.orders = orders;
        this.settlement = settlement;
    }

    @Transactional
    public boolean intentarCoincidencia(Orden nueva) {
        for (Orden pendiente : orders.findContrariasPendientes(nueva)) {
            if (pendiente.compatibleCon(nueva)) {
                settlement.liquidar(pendiente, nueva);
                return true;
            }
        }
        return false;
    }
}
