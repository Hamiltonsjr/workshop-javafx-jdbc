package model.services;

import model.dao.DaoFactory;
import model.dao.DepartmentDao;
import model.dao.SellerDao;
import model.entities.Department;
import model.entities.Seller;

import java.util.List;

public class SellerService {

    private SellerDao sellerDao = DaoFactory.createSellerDao();


    public List<Seller> findAll(){
        return sellerDao.findAll();
    }

    public void savedOrUpdate(Seller seller){
        if(seller.getId() == null){
            sellerDao.insert(seller);
        }
        else {
            sellerDao.update(seller);
        }
    }

    public void remove(Seller obj){
        sellerDao.deleteById(obj.getId());
    }

}
