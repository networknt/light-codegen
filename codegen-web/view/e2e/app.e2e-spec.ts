import { LightCodegenPage } from './app.po';

describe('light-codegen App', () => {
  let page: LightCodegenPage;

  beforeEach(() => {
    page = new LightCodegenPage();
  });

  it('should display message saying app works', () => {
    page.navigateTo();
    expect(page.getParagraphText()).toEqual('app works!');
  });
});
