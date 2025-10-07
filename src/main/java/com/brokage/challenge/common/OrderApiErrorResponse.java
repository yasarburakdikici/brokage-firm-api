package com.brokage.challenge.common;

import java.util.List;

public record OrderApiErrorResponse(String message, List<String> details) {

}
