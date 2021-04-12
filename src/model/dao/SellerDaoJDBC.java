package model.dao;

import db.DB;
import db.DbException;
import model.dao.SellerDao;
import model.entities.Department;
import model.entities.Seller;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SellerDaoJDBC implements SellerDao {

    private Connection connection;

    public SellerDaoJDBC(Connection connection){
        this.connection = connection;
    }

    @Override
    public void insert(Seller seller) {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

        try {
            preparedStatement = connection.prepareStatement("INSERT INTO seller\n" +
                    "(Name, Email, BirthDate, BaseSalary, DepartmentId)\n" +
                    "VALUES\n" +
                    "(?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);

            preparedStatement.setString(1, seller.getName());
            preparedStatement.setString(2,seller.getEmail());
            preparedStatement.setDate(3,new java.sql.Date(seller.getBirthDate().getTime()));
            preparedStatement.setDouble(4,seller.getBaseSalary());
            preparedStatement.setInt(5,seller.getDepartment().getId());

            int rowsAffected = preparedStatement.executeUpdate();

            if(rowsAffected > 0){
                ResultSet resultSet1 = preparedStatement.getGeneratedKeys();
                if (resultSet1.next()){
                    int id = resultSet1.getInt(1);
                    seller.setId(id);
                }
                DB.closeResultSet(resultSet1);
            }
            else {
                throw new DbException("Unexpected error! No rows affected!");
            }
        }
        catch (SQLException sqlException){
            throw new DbException(sqlException.getMessage());
        }
        finally {
            DB.closeStatement(preparedStatement);
        }
    }

    @Override
    public void update(Seller seller) {
        PreparedStatement preparedStatement = null;
        try{
            preparedStatement = connection.prepareStatement("UPDATE seller\n" +
                    "SET Name = ?, Email = ?, BirthDate = ?, BaseSalary = ?, DepartmentId = ?\n" +
                    "WHERE Id = ?");

            preparedStatement.setString(1,seller.getName());
            preparedStatement.setString(2,seller.getEmail());
            preparedStatement.setDate(3,new java.sql.Date(seller.getBirthDate().getTime()));
            preparedStatement.setDouble(4,seller.getBaseSalary());
            preparedStatement.setInt(5,seller.getDepartment().getId());
            preparedStatement.setInt(6,seller.getId());

            preparedStatement.executeUpdate();
        }
        catch (SQLException sqlException){
            throw new DbException(sqlException.getMessage());
        }
        finally {
            DB.closeStatement(preparedStatement);
        }
    }

    @Override
    public void deleteById(Integer id) {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement("DELETE FROM seller WHERE Id = ?");

            preparedStatement.setInt(1,id);
            int rows = preparedStatement.executeUpdate();

            if(rows == 0){
                throw new DbException("ID does not exist");
            }
        }
        catch (SQLException sqlException){
            throw new DbException(sqlException.getMessage());

        }
        finally {
            DB.closeStatement(preparedStatement);
        }
    }

    @Override
    public Seller findById(Integer id) {

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try{
            preparedStatement = connection.prepareStatement("" +
                    "SELECT seller.*, department.Name as DepName FROM seller INNER JOIN department ON seller.DepartmentId = department.id WHERE seller.Id = ?");

            preparedStatement.setInt(1,id);
            resultSet = preparedStatement.executeQuery();

            // Método para retorna um vendedro por ID
            // JDBC retorna uma lista e java retonar um objeto.
            // if(resultset.next()) pq no banco de dados começa na possição 0

            if(resultSet.next()){

                Department department = instantiateDepartment(resultSet);
                Seller seller = instantiateSeller(resultSet, department);
                return seller;
            }
            return null;
        }
        catch (SQLException sqlException){
            throw new DbException(sqlException.getMessage());

        }
        finally {
            DB.closeStatement(preparedStatement);
            DB.closeResultSet(resultSet);
        }
    }

    private Seller instantiateSeller(ResultSet resultSet, Department department) throws SQLException {
        Seller seller = new Seller();
        seller.setId(resultSet.getInt("Id"));
        seller.setName(resultSet.getString("Name"));
        seller.setEmail(resultSet.getString("Email"));
        seller.setBaseSalary(resultSet.getDouble("BaseSalary"));
        seller.setBirthDate(resultSet.getDate("BirthDate"));
        seller.setDepartment(department);

        return seller;
    }

    private Department instantiateDepartment(ResultSet resultSet) throws SQLException {
        Department department = new Department();
        department.setId(resultSet.getInt("DepartmentId"));
        department.setName(resultSet.getString("DepName"));
        return department;
    }

    @Override
    public List<Seller> findAll() {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            preparedStatement = connection.prepareStatement("SELECT seller.*,department.Name as DepName\n" +
                    "FROM seller INNER JOIN department\n" +
                    "ON seller.DepartmentId = department.Id\n" +
                    "ORDER BY Name");

            resultSet =preparedStatement.executeQuery();
            List<Seller> list = new ArrayList<>();
            Map<Integer, Department> map = new HashMap<>();

            while (resultSet.next()){
                Department dep = map.get(resultSet.getInt("DepartmentId"));

                if(map == null){
                    dep = instantiateDepartment(resultSet);
                    map.put(resultSet.getInt("DepartmentId"),dep);
                }
                Seller seller = instantiateSeller(resultSet,dep);
                list.add(seller);
            }
            return list;

        }
        catch (SQLException sqlException){
            throw new DbException(sqlException.getMessage());

        }
        finally {
            DB.closeStatement(preparedStatement);
            DB.closeResultSet(resultSet);
        }
    }

    @Override
    public List<Seller> findByDepartment(Department department) {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            preparedStatement = connection.prepareStatement("SELECT seller.*,department.Name as DepName\n" +
                    "FROM seller INNER JOIN department\n" +
                    "ON seller.DepartmentId = department.Id\n" +
                    "WHERE DepartmentId = ?\n" +
                    "ORDER BY Name");

            preparedStatement.setInt(1,department.getId());
            resultSet = preparedStatement.executeQuery();
            List<Seller> list = new ArrayList<>();
            Map<Integer, Department> map = new HashMap<>();

            // while para percorrer o resultSet até obter um próximo.
            while (resultSet.next()){
                // Testanto se o departamento já existe.
                Department dep  = map.get(resultSet.getInt("DepartmentId"));

                if(dep == null){
                    dep = instantiateDepartment(resultSet);
                    map.put(resultSet.getInt("DepartmentId"), dep);
                }

                Seller seller = instantiateSeller(resultSet, dep);
                list.add(seller);
            }
            return list;
        }
        catch (SQLException sqlException){
            throw new DbException(sqlException.getMessage());
        }
        finally {
            DB.closeStatement(preparedStatement);
            DB.closeResultSet(resultSet);
        }
    }
}

