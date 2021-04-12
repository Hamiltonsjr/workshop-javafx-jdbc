package model.dao;

import db.DB;
import db.DbException;
import model.dao.DepartmentDao;
import model.entities.Department;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DepartmentDaoJDBC implements DepartmentDao {

    private Connection connection;

    public DepartmentDaoJDBC(Connection connection){
        this.connection = connection;
    }

    @Override
    public void insert(Department department) {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try{
            preparedStatement = connection.prepareStatement("INSERT INTO department (Name) VALUES (?)");
            preparedStatement.setString(1,department.getName());

            int rowsAffected = preparedStatement.executeUpdate();

            if(rowsAffected > 0){
                ResultSet result = preparedStatement.getGeneratedKeys();
                if(result.next()){
                    int id = result.getInt(1);
                    department.setId(id);
                }else {
                    throw new DbException("Unexpected error! No rows affected");
                }
                DB.closeResultSet(result);
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
    public void update(Department department) {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement("UPDATE department SET Name = ? WHERE Id = ?");
            preparedStatement.setString(1,department.getName());
            preparedStatement.setInt(2,department.getId());

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
            preparedStatement = connection.prepareStatement("DELETE FROM department WHERE id = ? ");
            preparedStatement.setInt(1,id);

            int rows = preparedStatement.executeUpdate();

            if(rows == 0){
                throw new DbException("Id does not exist !");
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
    public Department findById(Integer id) {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement("SELECT * FROM department WHERE id = ?");
            preparedStatement.setInt(1,id);
            resultSet = preparedStatement.executeQuery();

            if(resultSet.next()){
                Department obj = new Department();
                obj.setId(resultSet.getInt("Id"));
                obj.setName(resultSet.getString("Name"));
                return obj;
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

    @Override
    public List<Department> findAll() {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement("SELECT * FROM department ORDER BY Name = ? ");
            resultSet = preparedStatement.executeQuery();

            List<Department> list = new ArrayList<>();
            while (resultSet.next()){
                Department obj = new Department();
                obj.setId(resultSet.getInt("id"));
                obj.setName(resultSet.getString("Name"));
                list.add(obj);
            }

            return list;

        }catch (SQLException sqlException){
            throw new DbException(sqlException.getMessage());
        }
        finally {
            DB.closeStatement(preparedStatement);
            DB.closeResultSet(resultSet);
        }
    }
}
