int g(int n)
{
  int r;
  r = 0;
  if (n <= 0) {
    r = 0;
  } else {
    r = g(n - 1);
    r = n + r;
  }
  return r;
}
int triangle(int n) {
  int r;
  r = g(n);
  return r;
}