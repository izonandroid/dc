package com.izontechnology.dcapp.downloadmanagerplus.interfaces;


import com.izontechnology.dcapp.downloadmanagerplus.enums.Errors;

/**
 * Created by Siamak on 17/02/2017.
 */

public interface ActionListener {

    void onSuccess();

    void onFailure(Errors error);
}
