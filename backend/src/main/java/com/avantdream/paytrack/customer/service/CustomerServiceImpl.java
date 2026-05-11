package com.avantdream.paytrack.customer.service;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.avantdream.paytrack.company.entity.Company;
import com.avantdream.paytrack.company.repository.CompanyRepository;
import com.avantdream.paytrack.customer.dto.CustomerRequest;
import com.avantdream.paytrack.customer.entity.Customer;
import com.avantdream.paytrack.customer.entity.CustomerStatus;
import com.avantdream.paytrack.customer.repository.CustomerRepository;
import com.avantdream.paytrack.shared.exception.ResourceNotFoundException;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CompanyRepository companyRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository, CompanyRepository companyRepository) {
        this.customerRepository = customerRepository;
        this.companyRepository = companyRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Customer findById(Long companyId, Long customerId) {
        return customerRepository.findByIdAndCompany_Id(customerId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", customerId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Customer> findAll(Long companyId, Pageable pageable) {
        return customerRepository.findByCompany_Id(companyId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Customer> search(Long companyId, String q) {
        return customerRepository.searchByCompany(companyId, q == null ? "" : q.trim());
    }

    @Override
    @Transactional
    public Customer create(Long companyId, CustomerRequest request) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", companyId));
        companyRepository.lockById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", companyId));
        long count = customerRepository.countByCompany_Id(companyId) + 1;
        Customer customer = new Customer();
        customer.setCompany(company);
        customer.setCustomerNumber(String.format("CUS-%05d", count));
        applyRequest(customer, request);
        return customerRepository.save(customer);
    }

    @Override
    @Transactional
    public Customer update(Long companyId, Long customerId, CustomerRequest request) {
        Customer customer = findById(companyId, customerId);
        applyRequest(customer, request);
        return customerRepository.save(customer);
    }

    @Override
    @Transactional
    public void delete(Long companyId, Long customerId) {
        Customer customer = findById(companyId, customerId);
        customer.setArchivedAt(new Date());
        customer.setStatus(CustomerStatus.ARCHIVED);
        customerRepository.save(customer);
    }

    private void applyRequest(Customer c, CustomerRequest r) {
        c.setName(r.getName().trim());
        c.setEmail(r.getEmail());
        c.setPhone(r.getPhone());
        c.setCompanyName(r.getCompanyName());
        c.setRegistrationNumber(r.getRegistrationNumber());
        c.setBillingAddressLine1(r.getBillingAddressLine1());
        c.setBillingAddressLine2(r.getBillingAddressLine2());
        c.setBillingCity(r.getBillingCity());
        c.setBillingState(r.getBillingState());
        c.setBillingPostcode(r.getBillingPostcode());
        c.setBillingCountry(r.getBillingCountry());
        c.setNotes(r.getNotes());
    }
}
