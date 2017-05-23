package com.excilys.training.persistence;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;


import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import com.excilys.training.dto.ComputerDTO;
import com.excilys.training.exception.NullComputerException;
import com.excilys.training.mapper.ComputerRowMapper;
import com.excilys.training.mapper.MapperComputer;
import com.excilys.training.model.Computer;
import com.excilys.training.model.QCompany;
import com.excilys.training.model.QComputer;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.hibernate.HibernateQueryFactory;
import com.querydsl.sql.SQLQueryFactory;

@Repository
public class JDBCTemplateComputer implements ComputerDAO {
    private static final String ADD_QUERY = "INSERT INTO computer(name,introduced,discontinued,company_id) VALUES(?,?,?,?)";
    private static final String DELETE_QUERY = "DELETE FROM computer WHERE id =?";
    private static final String FETCH_QUERY = "SELECT * FROM computer LIMIT ? OFFSET ? ";
    // fetch ordered query est incomplète. Cf l'implémentation de la requete
    // plus bas.
    private static final String FETCH_ORDERED_QUERY = "SELECT * FROM computer ORDER BY ";
    private static final String ID_QUERY = "SELECT * FROM computer WHERE id = ?";
    private static final String NAME_QUERY = "SELECT * FROM computer WHERE name LIKE ?";
    private static final String UPDATE_QUERY = "UPDATE computer SET name = ?, introduced = ?, discontinued = ?,company_id=? WHERE id = ?";
    private static final String COUNT_QUERY = "SELECT COUNT(*) FROM computer";

    private JdbcTemplate jdbcTemplate;
   
    
    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
    
    @Autowired
    private SQLQueryFactory queryFactory;
    
    @Autowired
    PlatformTransactionManager transactionManager;
    
    @Autowired
    JDBCTemplateCompany JdbcTemplateCompany;

    public void add(Computer obj) {

        System.out.println(obj);
        QComputer Qcomputer = QComputer.computer;
        QCompany Qcompany = QCompany.company;
        Long c = queryFactory.select(Qcomputer.id).where(Qcomputer.name.eq("CM-200")).from(Qcomputer).fetchOne();
        System.out.println(c);
        List<String> names = queryFactory.select(Qcomputer.name).from(Qcomputer).fetch();
        System.out.println(names);
        
        List<Tuple> result = queryFactory.select(Qcomputer.name,Qcomputer.introduced,Qcomputer.discontinued)
                .from(Qcomputer).fetch();
        System.out.println(result);
        
        System.out.println("merde");
        List<Computer> computerResult = queryFactory.select(Qcomputer).from(Qcomputer).leftJoin(Qcompany)
                .on(Qcompany.id.eq(Qcomputer.company.id)).limit(10).offset(10 * 3).fetch();
        
        System.out.println(computerResult);
        List<Computer> dtos = queryFactory.select(Projections.constructor(Computer.class, Qcomputer.name, Qcomputer.introduced, Qcomputer.discontinued)).from(Qcomputer).fetch();
                
        System.out.println(dtos);
        //List<Computer> computerList = queryFactory.select(computer.name).from(computer);
        //System.out.println(computerList);

       

    }

    public void delete(long id) {
        jdbcTemplate.update(DELETE_QUERY, id);
    }

    public void update(long index, Computer obj) {
        String name = obj.getName();
        String introduced = (obj.getIntroduced() != null) ? obj.getIntroduced().toString() : null;
        String discontinued = (obj.getDiscontinued() != null) ? obj.getDiscontinued().toString() : null;
        Long idCompany = (obj.getCompany() != null) ? obj.getCompany().getId() : null;
        jdbcTemplate.update(UPDATE_QUERY, name, introduced, discontinued, idCompany, index);

    }

    public ArrayList<Computer> fetchPage(int page, int itemPerPage) {

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(FETCH_QUERY, itemPerPage, (page - 1) * itemPerPage);
        return MapToArray(rows);
    }

    public Computer getById(long id) throws NullComputerException {
        Computer computer = null;

        computer = (Computer) jdbcTemplate.queryForObject(ID_QUERY, new Object[] { id }, new ComputerRowMapper());

        return computer;

    }

    public ArrayList<Computer> getByName(String name) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(NAME_QUERY, name + "%");
        return MapToArray(rows);
    }

    public long getCount() {
        return jdbcTemplate.queryForObject(COUNT_QUERY, Integer.class);
    }

    public ArrayList<Computer> fetchOrderedPage(int page, int itemPerPage, String orderBy, String way) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(FETCH_ORDERED_QUERY + orderBy + " " + way + " LIMIT "
                + itemPerPage + " OFFSET " + (page - 1) * itemPerPage);
        return MapToArray(rows);
    }

    public ArrayList<Computer> MapToArray(List<Map<String, Object>> rows) {
        ArrayList<Computer> arrayResults = new ArrayList<Computer>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (Map row : rows) {
            ComputerDTO computerDTO = new ComputerDTO();
            computerDTO.setId((long) row.get("id"));
            computerDTO.setName(row.get("name").toString());
            if (row.get("introduced") != null) {
                computerDTO
                        .setIntroduced(LocalDate.parse(row.get("introduced").toString(), formatter).format(formatter2));
            }
            if (row.get("discontinued") != null) {
                computerDTO.setDiscontinued(
                        LocalDate.parse(row.get("discontinued").toString(), formatter).format(formatter2));
            }

            if (row.get("company_id") != null) {
                long indexCompany = (long) row.get("company_id");
                computerDTO.setCompanyName(JdbcTemplateCompany.getById(indexCompany).getName());
            }
            arrayResults.add(MapperComputer.DTOToObj(computerDTO));
        }
        return arrayResults;
    }

}
