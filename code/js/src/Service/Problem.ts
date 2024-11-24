export type Problem = {
  type: string;
  title: string;
  status: number;
  detail: string;
  instance?: string;
};

export const problemMediaType = 'application/problem+json';

export function isProblem(object: object): object is Problem {
  return (
    typeof object === 'object' &&
    object !== null &&
    'typeUri' in object &&
    'title' in object &&
    'status' in object &&
    typeof object.status === 'number'
  );
}
