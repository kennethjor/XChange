package org.knowm.xchange.campbx.service.polling;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.campbx.CampBX;
import org.knowm.xchange.campbx.dto.CampBXOrder;
import org.knowm.xchange.campbx.dto.CampBXResponse;
import org.knowm.xchange.campbx.dto.trade.MyOpenOrders;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.MarketOrder;
import org.knowm.xchange.dto.trade.OpenOrders;
import org.knowm.xchange.dto.trade.UserTrades;
import org.knowm.xchange.exceptions.ExchangeException;
import org.knowm.xchange.exceptions.NotAvailableFromExchangeException;
import org.knowm.xchange.exceptions.NotYetImplementedForExchangeException;
import org.knowm.xchange.service.polling.trade.PollingTradeService;
import org.knowm.xchange.service.polling.trade.params.TradeHistoryParams;

/**
 * @author Matija Mazi
 */
public class CampBXTradeService extends CampBXTradeServiceRaw implements PollingTradeService {

  private final Logger logger = LoggerFactory.getLogger(CampBXTradeService.class);

  private static final MessageFormat ID_FORMAT = new MessageFormat("{0}-{1}");

  /**
   * Constructor
   *
   * @param exchange
   */
  public CampBXTradeService(Exchange exchange) {

    super(exchange);
  }

  @Override
  public OpenOrders getOpenOrders() throws IOException {

    MyOpenOrders myOpenOrders = getCampBXOpenOrders();
    logger.debug("myOpenOrders = {}", myOpenOrders);

    if (!myOpenOrders.isError()) {

      // TODO move to adapter class
      List<LimitOrder> orders = new ArrayList<LimitOrder>();
      for (CampBXOrder cbo : myOpenOrders.getBuy()) {
        if (cbo.isError() || cbo.isInfo()) {
          logger.debug("Skipping non-order in Buy: " + cbo);
        } else {
          String id = composeOrderId(CampBX.OrderType.Buy, cbo.getOrderID());
          BigDecimal price = cbo.getPrice();
          orders.add(new LimitOrder(Order.OrderType.BID, cbo.getQuantity(), CurrencyPair.BTC_USD, id, cbo.getOrderEntered(), price));
        }
      }
      for (CampBXOrder cbo : myOpenOrders.getSell()) {
        if (cbo.isError() || cbo.isInfo()) {
          logger.debug("Skipping non-order in Sell: " + cbo);
        } else {

          String id = composeOrderId(CampBX.OrderType.Sell, cbo.getOrderID());
          BigDecimal price = cbo.getPrice();
          orders.add(new LimitOrder(Order.OrderType.ASK, cbo.getQuantity(), CurrencyPair.BTC_USD, id, cbo.getOrderEntered(), price));
        }
      }
      return new OpenOrders(orders);
    } else {
      throw new ExchangeException("Error calling getOpenOrders(): " + myOpenOrders.getError());
    }
  }

  @Override
  public String placeMarketOrder(MarketOrder marketOrder) throws IOException {

    CampBXResponse campBXResponse = placeCampBXMarketOrder(marketOrder);
    logger.debug("campBXResponse = {}", campBXResponse);

    if (!campBXResponse.isError()) {
      return composeOrderId(campBXResponse.getSuccess(), marketOrder.getType());
    } else {
      throw new ExchangeException("Error calling placeMarketOrder(): " + campBXResponse.getError());
    }
  }

  @Override
  public String placeLimitOrder(LimitOrder limitOrder) throws IOException {

    CampBXResponse campBXResponse = placeCampBXLimitOrder(limitOrder);
    logger.debug("campBXResponse = {}", campBXResponse);

    if (!campBXResponse.isError()) {
      return composeOrderId(campBXResponse.getSuccess(), limitOrder.getType());
    } else {
      throw new ExchangeException("Error calling placeLimitOrder(): " + campBXResponse.getError());
    }
  }

  @Override
  public boolean cancelOrder(String orderId) throws IOException {

    CampBXResponse campBXResponse = cancelCampBXOrder(orderId);
    logger.debug("campBXResponse = {}", campBXResponse);

    if (!campBXResponse.isError()) {
      return campBXResponse.isSuccess();
    } else {
      throw new ExchangeException("Error calling cancelOrder(): " + campBXResponse.getError());
    }
  }

  private String composeOrderId(String id, Order.OrderType orderType) {

    CampBX.OrderType type = orderType == Order.OrderType.ASK ? CampBX.OrderType.Sell : CampBX.OrderType.Buy;
    return composeOrderId(type, id);
  }

  private String composeOrderId(CampBX.OrderType type, String id) {

    return ID_FORMAT.format(new Object[] { type, id });
  }

  @Override
  public UserTrades getTradeHistory(TradeHistoryParams params) throws IOException {

    throw new NotAvailableFromExchangeException();
  }

  @Override
  public org.knowm.xchange.service.polling.trade.params.TradeHistoryParams createTradeHistoryParams() {

    throw new NotAvailableFromExchangeException();
  }

  @Override
  public Collection<Order> getOrder(String... orderIds)
      throws ExchangeException, NotAvailableFromExchangeException, NotYetImplementedForExchangeException, IOException {
    throw new NotYetImplementedForExchangeException();
  }

}
