type HomeOutputModel = {
  id: string;
  username: string;
};

export type HomeOutput = HomeOutputModel;

export type UserListOutputModel = {
  users: Array<HomeOutputModel>;
};
