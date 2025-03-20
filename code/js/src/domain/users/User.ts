export type PasswordValidationInfo = {
  validationInfo: string;
};

export type User = {
  id: number;
  email: string;
  username: string;
  passwordValidation: PasswordValidationInfo;
};
