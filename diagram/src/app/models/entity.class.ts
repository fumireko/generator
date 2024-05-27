import { Attribute } from "./attribute.class";

export class Entity {
    constructor(
        public name: string,
        public attributes: Attribute[],
    ){}
}
