import React from 'react';
import { Link } from 'react-router-dom';

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

export function ProblemComponent({ problem }: { problem: Problem }) {
  return (
    <div>
      <h1><b>{problem.status}:</b>{problem.title}</h1>
      <p><b>Detail:</b>{problem.detail}</p>
      <Link to={problem.type}>{problem.type}</Link>
    </div>
  )
}