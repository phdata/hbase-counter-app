package io.phdata.examples;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class HBaseCounterServlet extends HttpServlet {
  private static final byte[] TABLE = Bytes.toBytes("hbase-counter-app");
  private static final byte[] ROW = Bytes.toBytes("cereals-counter");
  private static final byte[] VALUES = Bytes.toBytes("v");

  private Configuration config;
  private Connection connection;
  private Table table;

  public HBaseCounterServlet() {
    ensureConnection();
  }

  private void ensureConnection() {
    if (table == null) {
      createConnection();
    }
  }
  private void createConnection() {
    try {
      try {
        if (connection != null) {
          connection.close();
        }
      } catch (IOException ex) {
        // ignored
      }
      try {
        if (table != null) {
          table.close();
        }
      } catch (IOException ex) {
        // ignored
      }
      config = HBaseConfiguration.create();
      connection = ConnectionFactory.createConnection(config);
      table = connection.getTable(TableName.valueOf(TABLE));
    } catch (Exception ex) {
      ex.printStackTrace();
      throw new RuntimeException(ex);
    }
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    ensureConnection();
    String counter = request.getParameter("counter");
    if (counter != null) {
      counter = counter.trim().toLowerCase();
      if (!counter.isEmpty()) {
        table.incrementColumnValue(ROW, VALUES, Bytes.toBytes(counter), 1, Durability.ASYNC_WAL);
      }
    }
    Scan scan = new Scan();
    ResultScanner scanner = table.getScanner(scan);
    StringBuffer buffer = new StringBuffer("{ ");
    for (Result result : scanner) {
      Map<byte[], byte[]> values = result.getFamilyMap(VALUES);
      for (byte[] key : values.keySet()) {
        buffer.append(String.format("\"%s\": %d, ", Bytes.toString(key), Bytes.toLong(values.get(key))));
      }
    }
    scanner.close();
    response.setContentType("application/json");
    response.setStatus(HttpServletResponse.SC_OK);
    response.getWriter().println(buffer.append("\"dummy\": 0 }").toString());
  }
}
