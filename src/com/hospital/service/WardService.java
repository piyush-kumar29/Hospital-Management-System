package com.hospital.service;

import com.hospital.model.BedAllocation;
import com.hospital.repository.DataStore;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class WardService {
    private final DataStore dataStore = DataStore.getInstance();

    public List<BedAllocation> getAllBeds() {
        return new ArrayList<>(dataStore.getBeds());
    }

    public boolean allocateBed(String bedId, String patientId, String patientName, String staffUsername) {
        for (BedAllocation bed : dataStore.getBeds()) {
            if (bed.getId().equals(bedId)) {
                bed.setPatientId(patientId);
                bed.setPatientName(patientName);
                bed.setAdmissionDate(LocalDate.now().toString());
                bed.setStatus("OCCUPIED");
                dataStore.saveAllData();
                dataStore.addLog(staffUsername, "RECEPTION", "ALLOCATE_BED", "Allocated bed " + bed.getBedNumber() + " (" + bed.getWardName() + ") to " + patientName);
                return true;
            }
        }
        return false;
    }

    public boolean dischargeBed(String bedId, String staffUsername) {
        for (BedAllocation bed : dataStore.getBeds()) {
            if (bed.getId().equals(bedId)) {
                String prevPatient = bed.getPatientName();
                bed.setDischargeDate(LocalDate.now().toString());
                bed.setPatientId("");
                bed.setPatientName("");
                bed.setStatus("AVAILABLE");
                dataStore.saveAllData();
                dataStore.addLog(staffUsername, "STAFF", "DISCHARGE_BED", "Discharged patient " + prevPatient + " from bed " + bed.getBedNumber());
                return true;
            }
        }
        return false;
    }
}
