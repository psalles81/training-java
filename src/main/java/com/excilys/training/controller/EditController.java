package com.excilys.training.controller;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.excilys.training.dto.CompanyDTO;
import com.excilys.training.dto.ComputerDTO;
import com.excilys.training.mapper.MapperCompany;
import com.excilys.training.mapper.MapperComputer;
import com.excilys.training.model.Company;
import com.excilys.training.model.Computer;
import com.excilys.training.service.CompanyService;
import com.excilys.training.service.ComputerService;
import com.excilys.training.validator.ValidatorWeb;

@Controller
@RequestMapping("/editComputer")
public class EditController {
    @Autowired
    ComputerService computerServiceImp;
    @Autowired
    CompanyService companyServiceImp;

    @RequestMapping(method = RequestMethod.GET)
    public String add(ModelMap model, @RequestParam(value = "id") Long id) {
        ArrayList<CompanyDTO> companyListDTO = new ArrayList<CompanyDTO>();
        ArrayList<Company> companyList = new ArrayList<Company>();

        companyList = companyServiceImp.fetchAll();
        for (int i = 0; i < companyList.size(); i++) {
            companyListDTO.add(MapperCompany.ObjToDTO(companyList.get(i)));
        }

        model.addAttribute("companyList", companyListDTO);
        model.addAttribute("id", id);
        return "editComputer";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String printHello(ModelMap model, @RequestParam(value = "computerName", required = false) String name,
            @RequestParam(value = "id", required = false) Long id,
            @RequestParam(value = "introduced", required = false) String introduced,
            @RequestParam(value = "discontinued", required = false) String discontinued,
            @RequestParam(value = "companyId", required = false) Long companyId
            

    ) {

        ComputerDTO cdto = new ComputerDTO();
        Computer c = null;
        cdto.setId(id);
        cdto.setName(name);
        cdto.setIntroduced(introduced);
        cdto.setDiscontinued(discontinued);
        System.out.println(id);
        cdto.setCompanyName((companyId != null && companyId !=0) ? companyServiceImp.getById(companyId).getName() : null);
        if (ValidatorWeb.validName(name) && ValidatorWeb.validIntroduced(introduced)
                && ValidatorWeb.validDiscontinued(discontinued, introduced)) {
            c = MapperComputer.DTOToObj(cdto);
            System.out.println(computerServiceImp);
            computerServiceImp.update(id,c);
            System.out.println("mis a jour");
        }
        else { model.addAttribute("Erreur",4);}

        return "redirect:/dashboard";
    }
}