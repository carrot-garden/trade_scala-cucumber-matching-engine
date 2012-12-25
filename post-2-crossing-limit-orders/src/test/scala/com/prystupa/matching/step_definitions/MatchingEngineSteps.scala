package com.prystupa.matching.step_definitions

import scala.collection.JavaConversions._

import cucumber.api.java.en.{When, Then}
import cucumber.api.DataTable
import com.prystupa.matching._
import org.scalatest.matchers.ShouldMatchers

/**
 * Created with IntelliJ IDEA.
 * User: eprystupa
 * Date: 12/24/12
 * Time: 8:46 PM
 */

class MatchingEngineSteps extends ShouldMatchers {

  val buyBook = new OrderBook(Buy)
  val sellBook = new OrderBook(Sell)
  val matchingEngine = new MatchingEngine(buy = buyBook, sell = sellBook)

  var actualTrades = List[Trade]()


  @When("^the following orders are submitted in this order:$")
  def the_following_orders_are_submitted_in_this_order(orders: java.util.List[OrderRow]) {

    val trades = orders.toList.flatMap(o => matchingEngine.acceptOrder(
      LimitOrder(o.broker, parseSide(o.side), o.qty, o.price.toDouble)))
    actualTrades = actualTrades ::: trades
  }

  @Then("^market order book looks like:$")
  def market_order_book_looks_like(book: DataTable) {

    val (buyOrders, sellOrders) = parseExpectedBooks(book)

    buyBook.orders should equal(buyOrders)
    sellBook.orders should equal(sellOrders)
  }

  @Then("^the following trades are generated:$")
  def the_following_trades_are_generated(trades: java.util.List[Trade]) {

    actualTrades should equal(trades.toList)
    actualTrades = Nil
  }

  @Then("^no trades are generated$")
  def no_trades_are_generated() {

    actualTrades should equal(Nil)
  }


  private def parseExpectedBooks(book: DataTable): (List[Order], List[Order]) = {
    def buildOrders(orders: List[List[String]], side: Side) = {
      orders.filterNot(_.forall(_.isEmpty)).map(order => {
        val (broker :: qty :: price :: Nil) = order
        LimitOrder(broker, side, qty.toDouble, price.toDouble)
      })
    }

    val orders = book.raw().toList.drop(1).map(_.toList)
    val buy = orders.map(_.take(3))
    val sell = orders.map(_.drop(3).reverse)
    (buildOrders(buy, Buy), buildOrders(sell, Sell))
  }

  private def parseSide(s: String): Side = s.toLowerCase match {
    case "buy" => Buy
    case "sell" => Sell
  }

  private case class OrderRow(broker: String, side: String, qty: Double, price: String)

}