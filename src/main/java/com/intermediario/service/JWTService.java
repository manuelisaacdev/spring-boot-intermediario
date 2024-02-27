package com.intermediario.service;

import java.util.UUID;

import com.intermediario.exception.JWTServiceException;
import com.intermediario.model.Funcionario;

public interface JWTService {
	public String getType();
	public UUID extractSubjectAccessToken(String authorization);
	public UUID extractSubjectRefreshToken(String authorization);
	public String generateAccessToken(Funcionario funcionario);
	public String generateRefreshToken(Funcionario funcionario) throws JWTServiceException;
	public String extractToken(String authorization) throws JWTServiceException;
	public <T> T extractClaimAccessToken(String authorization, String claimName, Class<T> targetClass) throws JWTServiceException;
	public <T> T extractClaimRefreshToken(String authorization, String claimName, Class<T> targetClass) throws JWTServiceException;
}
