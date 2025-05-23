package org;

import org.database.DatabaseConnection;
import org.database.DatabaseSetup;

import java.sql.*;
import java.util.logging.Logger;

public class DietaryAndAllergies {
    private static final Logger logger = Logger.getLogger(DietaryAndAllergies.class.getName());

    private static DietaryAndAllergies instance;


    public static synchronized DietaryAndAllergies getInstance() {
        if (instance == null) {
            instance = new DietaryAndAllergies();
        }
        return instance;
    }
    private DietaryAndAllergies() {
     DatabaseSetup.setupDatabase();

    }


    public static int addNewCustomer(String dietary, String allergies, String email) {
        String sql = "INSERT INTO customer_preferences (dietary, allergies,email) VALUES (?, ?,?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            conn.setAutoCommit(false);

            stmt.setString(1, dietary.toLowerCase());
            stmt.setString(2, allergies.toLowerCase());
            stmt.setString(3,email);
            stmt.executeUpdate();

            ResultSet generatedKeys = stmt.getGeneratedKeys();
            int customerId = -1;
            if (generatedKeys.next()) {
                customerId = generatedKeys.getInt(1);
            }

            conn.commit();
            System.out.println("Customer preferences saved successfully! ID = " + customerId);
            return customerId;

        } catch (SQLException e) {
            logger.warning(e.getMessage());

            return -1;
        }

    }


    public static String getCustomerAllergies(int customerId) {
        String sql = "SELECT allergies FROM customer_preferences WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("allergies");
            }
        } catch (SQLException e) {
            logger.warning(e.getMessage());
        }
        return null;
    }
    public static String getCustomerPreferences(int customerId) {
        String sql = "SELECT dietary FROM customer_preferences WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("dietary");
            }
        } catch (SQLException e) {
            logger.warning(e.getMessage());
        }
        return null;
    }

    public static boolean checkAllergies(int customerId, String meal) throws SQLException {
        String allergies = getCustomerAllergies(customerId);
        if (allergies == null || allergies.isEmpty()||allergies.equalsIgnoreCase("none")) {
            return false;
        }
        String ingredientsQuery = "SELECT ingredient FROM meal_ingredients " +
                "WHERE menu_item_id = (SELECT id FROM menu_items WHERE name = ?)";

        try ( Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ingredientsStmt = conn.prepareStatement(ingredientsQuery)) {
            ingredientsStmt.setString(1,meal);
            try (ResultSet rs = ingredientsStmt.executeQuery()) {
                while (rs.next()) {
                    String ingredient = rs.getString("ingredient").toLowerCase();
                    if (ingredient.contains(allergies)) {
                        return true;
                    }
                }
            }
        } catch (SQLException ex) {
            throw new SQLException(ex);
        }

    return false ;
    }


}
